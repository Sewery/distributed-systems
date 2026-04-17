import express, { Request, Response } from 'express';
import axios from 'axios';
import path from 'path';
import { Astronaut, Launch } from './space';
import swaggerJsdoc from 'swagger-jsdoc';
import swaggerUi from 'swagger-ui-express';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import Joi from 'joi';
const app = express();
const PORT = process.env.PORT || 3000;

app.disable('x-powered-by');
app.use(helmet());

// Ograniczenie liczby żądań do API
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 100,
  standardHeaders: 'draft-8',
  legacyHeaders: false
});
app.use('/api', limiter);

app.use(express.urlencoded({ extended: true, limit: '10kb' }));
app.use(express.static(path.join(process.cwd(), 'public')));
app.set('view engine', 'ejs');
app.set('views', path.join(process.cwd(), 'views'));

app.get('/', (_req: Request, res: Response) => {
  res.sendFile(path.join(process.cwd(), 'public', 'index.html'));
});

// Walidacja danych wejściowych za pomocą Joi
const schema = Joi.object({
    daysLimit: Joi.number().integer().min(1).max(365).required(),
    minAstronauts: Joi.number().integer().min(0).default(0)
});

/**
 * @openapi
 * /api/space-summary:
 *   post:
 *     summary: Generuje raport aktywności kosmicznej
 *     description: Pobiera dane z Open Notify i Launch Library, liczy statystyki i zwraca HTML lub JSON.
 *     requestBody:
 *       required: true
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             required: [daysLimit]
 *             properties:
 *               daysLimit:
 *                 type: integer
 *                 minimum: 1
 *                 maximum: 365
 *                 example: 30
 *               minAstronauts:
 *                 type: integer
 *                 minimum: 0
 *                 example: 1
 *     responses:
 *       200:
 *         description: Raport wygenerowany poprawnie
 *       400:
 *         description: Błąd walidacji danych wejściowych
 *       502:
 *         description: Błąd komunikacji z zewnętrznym API
 */
app.post('/api/space-summary', async (req: Request, res: Response) => {
  try {

    const { value, error } = schema.validate(req.body, { convert: true });

    if (error) {
    return res.status(400).json({ error: error.message });
    }

    const daysLimit = value.daysLimit;
    const minAstronauts = value.minAstronauts;


    const [astrosRes, launchesRes] = await Promise.all([
      axios.get('http://api.open-notify.org/astros.json'),
      axios.get('https://lldev.thespacedevs.com/2.2.0/launch/upcoming/?limit=10')
    ]);

    const people: Astronaut[] = astrosRes.data.people ?? [];
    const launches: Launch[] = launchesRes.data.results ?? [];

    const craftStats: Record<string, number> = people.reduce((acc, person) => {
      acc[person.craft] = (acc[person.craft] ?? 0) + 1;
      return acc;
    }, {} as Record<string, number>);

    const filteredCraftStats = Object.fromEntries(
      Object.entries(craftStats).filter(([, count]) => count >= minAstronauts)
    );

    const locationCounts: Record<string, number> = {};
    for (const launch of launches) {
      const location = launch.pad?.location?.name ?? 'Nieznana lokalizacja';
      locationCounts[location] = (locationCounts[location] ?? 0) + 1;
    }

    let mostCommonLocation = 'Brak danych';
    let maxCount = 0;
    for (const [location, count] of Object.entries(locationCounts)) {
      if (count > maxCount) {
        maxCount = count;
        mostCommonLocation = location;
      }
    }

    const avgLaunchesPerDay = (launches.length / daysLimit).toFixed(2);

    // Renderowanie raportu
    return res.render('report', {
      totalPeople: astrosRes.data.number ?? people.length,
      craftStats: filteredCraftStats,
      mostCommonLocation,
      avgLaunchesPerDay,
      launches
    });
  } catch (error) {
    console.error(error);
    return res.status(502).send('Błąd zewnętrznego API. Spróbuj ponownie później.');
  }
});

// Komentarze Swaggera 
const swaggerSpec = swaggerJsdoc({
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Kosmiczny Analizator API',
      version: '1.0.0',
      description: 'API agregujące dane o astronautach i nadchodzących startach'
    },
    servers: [{ url: 'http://localhost:3000' }]
  },
  apis: ['./src/app.ts']
});

// Endpoint do dokumentacji Swaggera
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
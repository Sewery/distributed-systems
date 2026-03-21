import express from 'express';
import { Request,Response } from 'express';
import bcrypt from 'bcryptjs';
import Customer from './models/Customer';
import RefreshToken from './models/Refresh_token';
import { Op } from '@sequelize/core';
import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';
dotenv.config({ path: '../.env' });
import CustomerInterface from '../interfaces/CustomerInterface';
import AuthorizedMiddleware from '../authMiddleware';
import cors from 'cors';


const SECRET_KEY_ACCESS= process.env.SECRET_KEY_ACCESS as string;
const SECRET_KEY_REFRESH= process.env.SECRET_KEY_REFRESH as string;
const ACCESS_TOKEN_EXPIRES= process.env.ACCESS_TOKEN_EXPIRES as string;
const REFRESH_TOKEN_EXPIRES= process.env.REFRESH_TOKEN_EXPIRES as string;
const PORT=process.env.AUTH_SERVICE_PORT || "3030" as string;


const app = express();
app.use(express.json());
app.use(cors());

app.use(express.urlencoded({ extended: true })); // Do obsługi danych z formularzy HTML
app.use(express.static('public')); // Serwowanie pliku index.html


const axios = require('axios');

app.post('/api/space-summary', async (req, res) => {
    try {
        // 1. Walidacja danych wejściowych (np. przez Joi)
        const { daysLimit } = req.body; 
        if (!daysLimit || daysLimit <= 0) {
            return res.status(400).send("Podaj poprawną liczbę dni!");
        }

        // 2. Asynchroniczne pobieranie danych z dwóch źródeł
        const [astrosRes, launchesRes] = await Promise.all([
            axios.get('http://api.open-notify.org/astros.json'),
            axios.get(`https://lldev.thespacedevs.com/2.2.0/launch/upcoming/?limit=10`)
        ]);

        const people = astrosRes.data.people;
        const launches = launchesRes.data.results;

        // 3. OBRÓBKA DANYCH (To jest wysoko punktowane!)
        const stats = {
            totalPeople: astrosRes.data.number,
            byCraft: {}, // Ile osób na jakim statku
            upcomingLaunchesCount: launches.filter(l => /* logika sprawdzania daty */ true).length,
            mostPopularLocation: "" // Logika znalezienia najczęstszego kosmodromu
        };

        // Przykład prostej obróbki - grupowanie po statku
        people.forEach(p => {
            stats.byCraft[p.craft] = (stats.byCraft[p.craft] || 0) + 1;
        });

        // 4. Generowanie odpowiedzi (Renderowanie strony EJS)
        res.render('report', { stats, launches });

    } catch (error) {
        // 5. Obsługa błędów (Punktacja 0-9)
        console.error(error);
        res.status(502).render('error', { message: "Błąd komunikacji z API kosmicznym." });
    }
});

app.post('/api/login', async (req:Request, res:Response) => {
    const {email,password}=req.body;
    if(!email || !password){
        res.status(400).json({message:'Please fill all the fields'});
        return;
    }
    const customer=await Customer.findOne({ where: { email: email } });
    if(!customer){
        res.status(404).json({message:'User not found'});
        return;
    }
    const isPasswordCorrect=await bcrypt.compare(password,customer.password);
    if(!isPasswordCorrect){
        res.status(401).json({ message:'Invalid credentials'});
        return;

    }
    const accessToken=jwt.sign({email:email,id:customer.customer_id,is_admin:customer.is_admin},SECRET_KEY_ACCESS,{expiresIn:Number(ACCESS_TOKEN_EXPIRES)});
    const refreshToken=jwt.sign({email:email,id:customer.customer_id,is_admin:customer.is_admin},SECRET_KEY_REFRESH,{expiresIn:Number(REFRESH_TOKEN_EXPIRES)});
    await RefreshToken.create({token:refreshToken,expiry_date:new Date(Date.now() + Number(REFRESH_TOKEN_EXPIRES) * 1000)});
    res.status(200).json({accessToken:accessToken,refreshToken:refreshToken});
});


app.post('/api/refresh', async (req:Request, res:Response) => {
    const refreshToken=req.body.refreshToken;
    if(!refreshToken){
        res.status(400).json({message:'No token'});
        return;
    }
    try {
        const customer = jwt.verify(refreshToken, SECRET_KEY_REFRESH) as CustomerInterface;
        const token = await RefreshToken.findOne({ where: { token: refreshToken, expiry_date: { [Op.gt]: new Date() } } });
        if (!token) {
            res.status(403).json({message:'Invalid token'});
            return;
        }

        const accessToken = jwt.sign({ email: customer.email, id: customer.id ,is_admin:customer.is_admin}, SECRET_KEY_ACCESS, { expiresIn: ACCESS_TOKEN_EXPIRES });
        res.status(200).json({ accessToken: accessToken });
    } catch (err) {
        res.status(403).json({message:'Invalid token'});
    }

});

app.post('/api/logout', async (req:Request, res:Response) => {
    const refreshToken=req.body.refreshToken;
    if(!refreshToken){
        res.status(400).json({message:'No token'});
        return;
    }
    const token= await RefreshToken.findOne({ where: { token:refreshToken ,expiry_date: { [Op.gt]: new Date() } } })
    if(!token)
    {
        res.status(403).json({message:'Invalid token'});
        return;
    }

    await RefreshToken.destroy({ where: { token:refreshToken } });
    res.status(200).json({message:'Logged out'});

});

app.get('/test',AuthorizedMiddleware,(_,res:Response)=>{
    res.status(200).json({message:'Authorized'});
})





app.listen(parseInt(PORT,10), () => {
    console.log(`Server is running on port ${PORT} `);
});


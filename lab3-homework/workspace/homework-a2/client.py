import threading
import time
import uuid

import grpc

try:
	import logistics_pb2
	import logistics_pb2_grpc
except ImportError:
	print("Brak wygenerowanych plikow Python z proto.")
	raise


HOST = "host.docker.internal"
PORT = 50052


subscriptions = {}
subs_lock = threading.Lock()


def parse_vehicle_type(value):
	text = value.strip().upper()
	if not text:
		return logistics_pb2.UNKNOWN

	aliases = {
		"ANY": "UNKNOWN",
		"VAN": "DELIVERY_VAN",
		"TRUCK": "HEAVY_TRUCK",
		"PLANE": "CARGO_PLANE",
	}
	text = aliases.get(text, text)

	if not hasattr(logistics_pb2, text):
		raise ValueError("Nieznany typ pojazdu")
	return getattr(logistics_pb2, text)


def parse_area_codes(value):
	if not value.strip():
		return []
	return [code.strip().upper() for code in value.split(",") if code.strip()]


def format_alert(event):
	which = event.WhichOneof("alert")
	if which == "delay_reason":
		return f"delay_reason={event.delay_reason}"
	if which == "sensor_threshold":
		return f"sensor_threshold={event.sensor_threshold}"
	if which == "security_alert":
		return f"security_alert={event.security_alert}"
	return "alert=none"


def vehicle_name(value):
	try:
		return logistics_pb2.VehicleType.Name(value)
	except ValueError:
		return str(value)


class SubscriptionWorker(threading.Thread):
	def __init__(self, stub, request, stop_event):
		super().__init__(daemon=True)
		self.stub = stub
		self.request = request
		self.stop_event = stop_event

	def run(self):
		sub_id = self.request.subscription_id
		had_error = False
		while not self.stop_event.is_set():
			try:
				stream = self.stub.SubscribeTransport(self.request)
				if had_error:
					print(f"\n[{sub_id}] Polaczenie wroclo")
					print("> ", end="", flush=True)
					had_error = False
				for event in stream:
					if self.stop_event.is_set():
						break

					location = event.current_location
					print(
						"\n"
						f"[{sub_id}] {event.shipment_id} {vehicle_name(event.vehicle)} "
						f"{location.area_code}/{location.city} t={event.event_unix_time} "
						f"{format_alert(event)}"
					)
					print("> ", end="", flush=True)

				if self.stop_event.is_set():
					break

				had_error = True
				time.sleep(2)
			except grpc.RpcError as err:
				if self.stop_event.is_set():
					break
				had_error = True
				print(f"\n[{sub_id}] Utrata polaczenia ({err.code().name}), ponawiam...")
				print("> ", end="", flush=True)
				time.sleep(2)


def create_stub(host, port):
	channel = grpc.insecure_channel(
		f"{host}:{port}",
		options=[
			("grpc.keepalive_time_ms", 20000),
			("grpc.keepalive_timeout_ms", 10000),
			("grpc.keepalive_permit_without_calls", 1),
		],
	)
	return channel, logistics_pb2_grpc.LogisticsMonitorStub(channel)


def subscribe(stub, client_id):
	sub_id = input("subscription_id (puste = auto): ").strip() or f"sub-{uuid.uuid4().hex[:6]}"
	vehicle_text = input("typ pojazdu [UNKNOWN/DELIVERY_VAN/HEAVY_TRUCK/CARGO_PLANE lub ANY]: ")
	areas_text = input("area_codes (np. WAW,KRK; puste = wszystkie): ")

	try:
		vehicle_type = parse_vehicle_type(vehicle_text)
		area_codes = parse_area_codes(areas_text)
	except ValueError as ex:
		print(f"Blad danych: {ex}")
		return

	request = logistics_pb2.SubscriptionRequest(
		client_id=client_id,
		subscription_id=sub_id,
		type_filter=vehicle_type,
		area_codes=area_codes,
	)

	stop_event = threading.Event()
	worker = SubscriptionWorker(stub, request, stop_event)

	with subs_lock:
		if sub_id in subscriptions:
			print("Taki subscription_id juz istnieje.")
			return
		subscriptions[sub_id] = {
			"stop_event": stop_event,
			"worker": worker,
			"vehicle": vehicle_type,
			"areas": area_codes,
		}

	worker.start()
	print(f"Subskrypcja uruchomiona: {sub_id} (client_id={client_id})")


def unsubscribe(stub, client_id):
	sub_id = input("subscription_id do usuniecia: ").strip()
	if not sub_id:
		print("Podaj subscription_id.")
		return

	try:
		response = stub.UnsubscribeTransport(
			logistics_pb2.UnsubscribeRequest(
				client_id=client_id,
				subscription_id=sub_id,
			)
		)
		print(f"Serwer: removed={response.removed}, message={response.message}")
	except grpc.RpcError as err:
		print(f"Blad RPC przy unsubscribe: {err.code().name}")

	with subs_lock:
		data = subscriptions.pop(sub_id, None)
	if data:
		data["stop_event"].set()


def list_subscriptions():
	with subs_lock:
		if not subscriptions:
			print("Brak aktywnych subskrypcji.")
			return

		print("Aktywne subskrypcje:")
		for sub_id, data in subscriptions.items():
			print(f"- {sub_id}, vehicle={vehicle_name(data['vehicle'])}, areas={data['areas']}")


def shutdown(channel):
	with subs_lock:
		items = list(subscriptions.values())
		subscriptions.clear()
	for data in items:
		data["stop_event"].set()
	channel.close()


def print_help():
	print("\nDostepne komendy:")
	print("  subscribe - dodaj nowa subskrypcje")
	print("  unsubscribe - usun subskrypcje")
	print("  list - pokaz aktywne subskrypcje")
	print("  help - pokaz pomoc")
	print("  exit - zakoncz program")


def main():
	client_id = f"cli-{uuid.uuid4().hex[:8]}"
	channel, stub = create_stub(HOST, PORT)
	print(f"Polaczono do {HOST}:{PORT}, client_id={client_id}")
	print_help()

	try:
		while True:
			cmd = input("> ").strip().lower()
			if cmd == "subscribe":
				subscribe(stub, client_id)
			elif cmd == "unsubscribe":
				unsubscribe(stub, client_id)
			elif cmd == "list":
				list_subscriptions()
			elif cmd == "help":
				print_help()
			elif cmd in ("exit", "quit", "x"):
				break
			elif not cmd:
				continue
			else:
				print("Nieznana komenda. Wpisz 'help'.")
	except KeyboardInterrupt:
		pass
	finally:
		shutdown(channel)
		print("Koniec.")


if __name__ == "__main__":
	main()

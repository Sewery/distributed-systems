import socket;

serverIP = "127.0.0.1"
serverPort = 9008
msg = (300).to_bytes(4, byteorder='little')

print('PYTHON UDP CLIENT')
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.sendto(msg, (serverIP, serverPort))

# Odebranie wieadomosci od servera
buff, address = client.recvfrom(4)
print("python udp server received msg: ", int.from_bytes(buff, byteorder='little'))




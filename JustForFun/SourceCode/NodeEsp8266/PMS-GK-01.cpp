#include <Arduino.h>

#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WebSocketsClient.h>

ESP8266WiFiMulti WiFiMulti;
WebSocketsClient webSocket;

#include <Wire.h>
#include <SPI.h>
#include <Adafruit_PN532.h>

#define PN532_SCK  D1
#define PN532_MISO D2
#define PN532_MOSI D4
#define PN532_SS   D5

Adafruit_PN532 nfc(PN532_SCK, PN532_MISO, PN532_MOSI, PN532_SS);
bool webSocketConnected = false;

/*
 *
 */
void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {

	switch(type) {
		case WStype_DISCONNECTED:
			Serial.println("[WSc] Disconnected!\n");

			break;
		case WStype_CONNECTED: {
			Serial.print("[WSc] Connected to url: ");
			Serial.println((char*)payload);

			// send message to server when Connected
			webSocket.sendTXT("Connected");

			webSocketConnected = true;
		}
			break;
		case WStype_TEXT:
			Serial.printf("[WSc] get text: ");
			Serial.println((char*)payload);

			// send message to server
			// webSocket.sendTXT("message here");
			break;
		case WStype_BIN:
			Serial.printf("[WSc] get binary length: ");
			Serial.println(length);
			hexdump(payload, length);

			// send data to server
			// webSocket.sendBIN(payload, length);
			break;
	}

}

/*
 *
 */
void setupWifi() {
	Serial.println("Connecting wifi ...");
//	WiFiMulti.addAP("Siprit of fire", "cotranostra");
	WiFiMulti.addAP("HoiPhatCuongTraiXinhA215", "motdenmuoi1");

	//WiFi.disconnect();
	while(WiFiMulti.run() != WL_CONNECTED) {
		delay(10);
	}

	Serial.println("Connected");
}

/*
 *
 */
void setupWebSocket() {
	Serial.println("Setup websocket - start");
	// server address, port and URL
//	webSocket.begin("192.168.100.4", 8080, "/");
	webSocket.begin("nfc.ddns.net", 8080, "/");

	// event handler
	webSocket.onEvent(webSocketEvent);

	// use HTTP Basic Authorization this is optional remove if not needed
//	webSocket.setAuthorization("user", "Password");

	// try ever 5000 again if connection has failed
	webSocket.setReconnectInterval(2000);

	while(!webSocketConnected) {
		webSocket.loop();
		delay(5);
	}

	Serial.println("Setup websocket - stop");
}

/*
 *
 */
void setupPN532() {
	Serial.println("Setup pn532 - start");

	nfc.begin();

	uint32_t versiondata = nfc.getFirmwareVersion();
	if (! versiondata) {
	Serial.print("Didn't find PN53x board");
	// halt
	while (1)
		delay(10);
	}

	nfc.SAMConfig();

	Serial.println("Setup pn532 - stop");
	Serial.println("Waiting for an ISO14443A Card ...");
}

/*
 *
 */
void setup() {
	Serial.begin(115200);
	Serial.println();
//	Serial.setDebugOutput(true);

	setupWifi();

	setupWebSocket();

	setupPN532();
}

/*
 *
 */
void loop() {
	// Buffer to store the returned UID
	uint8_t uid[] = { 0, 0, 0, 0, 0, 0, 0 };
	// Length of the UID (4 or 7 bytes depending on ISO14443A card type)
	uint8_t uidLength;

	if(nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, uid, &uidLength)) {
	    // Display some basic information about the card
	    Serial.println("Found an ISO14443A card");
	    Serial.print("  UID Length: ");Serial.print(uidLength, DEC);Serial.println(" bytes");
	    Serial.print("  UID Value: ");
	    nfc.PrintHex(uid, uidLength);

	    if (uidLength == 4)
	    {
	      // We probably have a Mifare Classic card ...
	      uint32_t cardid = uid[0];
	      cardid <<= 8;
	      cardid |= uid[1];
	      cardid <<= 8;
	      cardid |= uid[2];
	      cardid <<= 8;
	      cardid |= uid[3];
	      Serial.print("Seems to be a Mifare Classic card #");
	      Serial.println(cardid);
	    }
	    Serial.println("");

	    //{\"park_name\":\"BigDick\",\"uid\":\"\"}
//	    String sendData = "{\"dick_name\":\"";
	    String sendData = "{\"park_name\":\"BigDick\",\"uid\":\"";
	    for(int i = 0; i < uidLength; i++) {
	    	sendData.operator +=(uid[i]);
	    	sendData.operator +=("_");
	    }
	    sendData.operator +=("\"}");
	    webSocket.sendTXT(sendData);
	}

	webSocket.loop();
//	Serial.println("abc\n");
}

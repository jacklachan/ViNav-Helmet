#include <Arduino.h>
#include <U8g2lib.h>
#include <Wire.h>
#include <BluetoothSerial.h>

// 1. HARDWARE SETUP: SH1106 1.3" OLED in Portrait Mode (R1)
// R1 = 90 degree rotation. Logical width is 64, height is 128.
U8G2_SH1106_128X64_NONAME_F_HW_I2C u8g2(U8G2_R1, /* reset=*/ U8X8_PIN_NONE);

BluetoothSerial SerialBT;

void drawNavUI(String distance, char direction) {
  u8g2.clearBuffer();
  
  // 2. THE LEAK FIX: Clears random "snow" pixels on SH1106 hardware edges
  u8g2.setDrawColor(0); 
  u8g2.drawBox(-2, -2, 68, 132); 
  u8g2.setDrawColor(1); 

  // 3. LARGE DIRECTIONAL ARROWS (Top of screen)
  if (direction == 'F') { // FORWARD
    u8g2.drawTriangle(32, 5, 10, 45, 54, 45); // Large Arrow Head
    u8g2.drawBox(25, 45, 14, 30);              // Thick Stem
  } 
  else if (direction == 'L') { // LEFT
    u8g2.drawTriangle(5, 40, 35, 10, 35, 70); 
    u8g2.drawBox(35, 30, 20, 20);              
  }
  else if (direction == 'R') { // RIGHT
    u8g2.drawTriangle(59, 40, 29, 10, 29, 70); 
    u8g2.drawBox(9, 30, 20, 20);               
  }
  else if (direction == 'U') { // U-TURN
    u8g2.drawCircle(32, 45, 20, U8G2_DRAW_UPPER_RIGHT | U8G2_DRAW_UPPER_LEFT);
    u8g2.drawTriangle(12, 45, 5, 65, 25, 65);
  }
  else if (direction == 'A') { // ARRIVED
    u8g2.setFont(u8g2_font_open_iconic_check_4x_t);
    u8g2.drawGlyph(16, 65, 64);
  }

  // 4. DISTANCE TEXT (Large font, bottom-centered)
  u8g2.setFont(u8g2_font_logisoso24_tf); 
  String displayStr = distance;
  int strWidth = u8g2.getStrWidth(displayStr.c_str());
  u8g2.drawStr((64 - strWidth) / 2, 110, displayStr.c_str());

  // 5. UNIT LABEL
  u8g2.setFont(u8g2_font_ncenB08_tr);
  u8g2.drawStr(28, 125, "meters");

  u8g2.sendBuffer();
}

void setup() {
  Serial.begin(115200);
  
  // Start I2C on standard ESP32 pins (SDA=21, SCL=22)
  Wire.begin(21, 22); 
  
  u8g2.begin();
  u8g2.setContrast(255); // Max brightness
  
  // Bluetooth Name
  SerialBT.begin("ViNav_Helmet"); 
  
  // Show Startup Screen
  u8g2.clearBuffer();
  u8g2.setFont(u8g2_font_ncenB10_tr);
  u8g2.drawStr(15, 60, "ViNav");
  u8g2.drawStr(10, 80, "Ready!");
  u8g2.sendBuffer();
  
  Serial.println("ViNav Helmet Started. Waiting for App...");
}

void loop() {
  // Check for incoming Bluetooth data
  if (SerialBT.available()) {
    String btData = SerialBT.readStringUntil('\n');
    btData.trim(); 

    // Expects "Direction,Distance" -> example: "L,450"
    int commaIndex = btData.indexOf(',');
    if (commaIndex > 0) {
      char dir = btData.substring(0, commaIndex).charAt(0);
      String dist = btData.substring(commaIndex + 1);
      
      // Update the screen
      drawNavUI(dist, dir);
      
      // Print to Serial for debugging
      Serial.print("Nav Update: Dir="); Serial.print(dir);
      Serial.print(" Dist="); Serial.println(dist);
    }
  }
}
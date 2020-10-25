#include <Servo.h>


/* Servos on both axis to later controll the attached camera */
Servo xAxisServo;
Servo yAxisServo;

/* Rotation on the x-Axis */
int currentX = 0;

/* Rotation on the y-Axis */
int currentY = 0;

/* Modes to indentify servo */
const int SERVO_X = 88;
const int SERVO_Y = 89;
int currentMode = 0;


/* Utility variables */
const int DELTA_ANGLE = 30; // Angle threshold
const int MIN_ROTATION = 15; //Minimum rotation for every angle

/* Simple variables for line reading */
String inputString = "";         // a String to hold incoming data
bool stringComplete = false;  // whether the string is complete
String in = "";


/* Setup */
void setup() {
  // initialize serial:
  Serial.begin(9600);
  // reserve 200 bytes for the inputString:
  inputString.reserve(200);

  xAxisServo.attach(9);  // attaches the servo on pin 9 to the servo object
  xAxisServo.write(currentX); //Reset the servo to it's native position
  yAxisServo.attach(10);
  yAxisServo.write(currentY);
}

void loop() {
  //Check if new Line is ready
  if (stringComplete) {
    const bool xMode = currentMode == SERVO_X;
    Servo current = xMode ? xAxisServo : yAxisServo;
    
    //Store input String, converted to a floating point number, in int "angle"
    int angleToRotate = inputString.toFloat();
    
    Serial.print("Rotating to angle: ");
    Serial.println(angleToRotate);
    //Calculate difference between old and given angle
    const int diff = xMode ? currentX - angleToRotate : currentY- angleToRotate;
    int tempAngle = diff < 0 ? diff * -1 : diff;
    //If the difference between old and new angle is higher than a certian threshold & not 0, adjust accordingly
    if (tempAngle <= MIN_ROTATION && tempAngle != 0) {
      //Calculate angle and store in tempAngle (int)
      tempAngle = angleToRotate > 90 ? angleToRotate - DELTA_ANGLE : angleToRotate + DELTA_ANGLE;
      //Print angle that is rotated to first, then rotate to given angle
      Serial.print("Rotation given lower than threshold, adjusting: ");
      Serial.println(tempAngle);
      current.write(tempAngle);
      //Delay
      delay(200);
    }
    //Change server position and store current
    current.write(angleToRotate);
    currentX = xAxisServo.read();
    currentY = yAxisServo.read();


    
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
}

/*
  SerialEvent occurs whenever a new data comes in the hardware serial RX. This
  routine is run between each time loop() runs, so using delay inside loop can
  delay response. Multiple bytes of data may be available.
*/
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    in += inChar;
    // if the incoming character is a newline, set a flag so the main loop can
    // do something about it:
    if (inChar == '\n') {
      currentMode = getValue(in, ':', 0).toInt();
      inputString = getValue(in, ':', 1);
      in = "";
      stringComplete = true;
    }
  }
}

String getValue(String data, char separator, int index)
{
    int found = 0;
    int strIndex[] = { 0, -1 };
    int maxIndex = data.length() - 1;

    for (int i = 0; i <= maxIndex && found <= index; i++) {
        if (data.charAt(i) == separator || i == maxIndex) {
            found++;
            strIndex[0] = strIndex[1] + 1;
            strIndex[1] = (i == maxIndex) ? i+1 : i;
        }
    }
    return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}

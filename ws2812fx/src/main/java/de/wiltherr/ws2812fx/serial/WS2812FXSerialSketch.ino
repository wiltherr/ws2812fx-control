
//Paket serial
#include <PacketSerial.h>
PacketSerial packetSerial;

//WS2812
#include <WS2812FX.h>
#define LED_PIN   6 // digital pin used to drive the LED strip (esp8266)
#define LED_COUNT 288 // number of LEDs on the strip
WS2812FX ws2812fx = WS2812FX(LED_COUNT, LED_PIN, NEO_GRBW + NEO_KHZ800);


struct Color {
  uint8_t r;
  uint8_t g;
  uint8_t b;
  uint8_t w;
};

struct UpdateSingleSegmentResponse {
  uint32_t bytesReceived;
};


#define ERROR_RESPONSE 255
#define FUNCTION_NOT_FOUND_RESPONSE 254
struct DefaultResponse {
  uint8_t functionNumber;
};



struct SegmentConfig {
  uint16_t _speed;
  uint8_t _mode;
  uint8_t _options;
  uint32_t _colors[NUM_COLORS];
};

void setup() {
  // init packet serial
//  packetSerial.begin(9600);
  packetSerial.begin(115200);

  packetSerial.setPacketHandler(&onPacketReceived);

  // init LED strip with some default segments
  ws2812fx.init();
  //init neopixelbus after ws2812fx
  //strip.Begin();
  //setting custom show function
  //ws2812fx.setCustomShow(myCustomShow);

  ws2812fx.setBrightness(5);
  //const uint32_t warmwhite = ws2812fx.Color(0,0,0,255);
  // parameters:  index, start,        stop,         mode,                              colors, speed, reverse
  ws2812fx.setSegment(0,     ((LED_COUNT/4)*0), ((LED_COUNT/4)*1)-1, FX_MODE_RAINBOW_CYCLE, (const uint32_t[]) {0xFF000000,0x000000FF,BLACK},  10, false);
  ws2812fx.setSegment(1,     ((LED_COUNT/4)*1), ((LED_COUNT/4)*2)-1, FX_MODE_RAINBOW_CYCLE, (const uint32_t[]) {RED,0x000000FF,BLACK},  10, true);
  ws2812fx.setSegment(2,     ((LED_COUNT/4)*2), ((LED_COUNT/4)*3)-1, FX_MODE_RAINBOW_CYCLE, (const uint32_t[]) {BLUE,0x000000FF,BLACK},  10, false);
  ws2812fx.setSegment(3,     ((LED_COUNT/4)*3), LED_COUNT-1        , FX_MODE_RAINBOW_CYCLE, (const uint32_t[]) {ORANGE,0x000000FF,BLACK},  10, true);
  ws2812fx.start();
}

uint8_t readyPacket[1] = {155};
void loop() {


//  if(!Serial.available() && !isPacketProcessing)   ws2812fx.service();  //this is breaking my signal sent back
//  ws2812fx.service();  //this is breaking my signal sent back
  packetSerial.update();
  packetSerial.send((const byte *)&readyPacket, sizeof(readyPacket));
  packetSerial.update();
  ws2812fx.service();
  packetSerial.update();
}

void onPacketReceived(const uint8_t* buffer, size_t size)
{

  // In this example, we will simply reverse the contents of the array and send
  // it back to the sender.

   // Make a temporary buffer.

  uint8_t functionNumber = (uint8_t) buffer[0];
  if(functionNumber == (uint8_t) 1) { //1 = SetState
        setStateRequest(buffer);
  } else if(functionNumber == (uint8_t) 2) { //2 = UpdateSegmentConfig

  } else if(functionNumber == (uint8_t) 3) { //3 = UpdateSegmentConfigAll
    requestUpdateSegmentConfigAll(buffer);
  } else if(functionNumber == (uint8_t) 4) { //4 = ResetSegments
     requestResetSegments(buffer);
  } else if(functionNumber == (uint8_t) 5) { //5 = GetSegments
      requestGetSegments();
   } else if(functionNumber == (uint8_t) 6) { //6 = GetSegment

  }  else if (functionNumber == (uint8_t) 7) { //7 = SetPixelColor

  }  else if (functionNumber == (uint8_t) 8) {

  } else if(functionNumber == (uint8_t) 9) { //10 = SetBrightness
    requestSetBrightness(buffer);
  } else if(functionNumber == (uint8_t) 10) { //10 = UpdateSegmentConfigMulti
    requestUpdateSegmentConfigMulti(buffer);
  } else if(functionNumber == (uint8_t) 11) { //11 = getLength
    requestGetLength();
  } else if(functionNumber == (uint8_t) 12) { //12 = setLength
    requestSetLength(buffer);
  } else {
    sendDefaultResponse(FUNCTION_NOT_FOUND_RESPONSE);
     packetSerial.send(buffer, size);
  }

}

void sendDefaultResponse(uint8_t _functionNumber) {
    DefaultResponse response;
    response.functionNumber = _functionNumber;
    packetSerial.send((const byte *)&response, sizeof(response));
}

/******* SetState ***********/

#define STATE_STOP 0
#define STATE_START 1
#define STATE_PAUSE 2
#define STATE_RESUME 3

struct SetStateParams {
  uint8_t functionNumber;
  uint8_t state;
};

void setStateRequest(byte* requestBytes) {
  SetStateParams params;
    memcpy(&params, requestBytes, sizeof(params));
    switch(params.state) {
      case STATE_STOP:
        ws2812fx.stop();
        break;
      case STATE_START:
        ws2812fx.start();
        break;
      case STATE_PAUSE:
        ws2812fx.pause();
        break;
      case STATE_RESUME:
        ws2812fx.resume();
        break;
      default:
        sendDefaultResponse(ERROR_RESPONSE);
        return;
    }
    sendDefaultResponse(params.functionNumber);
}

/******* GetLength ***********/

void requestGetLength() {
  uint16_t _length = ws2812fx.getLength();
  packetSerial.send((const byte *)&_length, sizeof(_length));
}

/******* SetLength ***********/

struct SetLengthRequest {
  uint8_t functionNumber;
  uint16_t _length;
};

void requestSetLength(byte* requestBytes) {
  SetLengthRequest params;
  memcpy(&params, requestBytes, sizeof(params));

  ws2812fx.setLength(params._length);
  ws2812fx.stop();
  ws2812fx.start();
  sendDefaultResponse(params.functionNumber);
}

/******* SetBrightness ***********/
struct SetBrightnessRequest {
  uint8_t functionNumber;
  uint8_t brightness;
};

void requestSetBrightness(byte* requestBytes) {
  SetBrightnessRequest params;
  memcpy(&params, requestBytes, sizeof(params));
  ws2812fx.setBrightness(params.brightness);
  ws2812fx.show();
  sendDefaultResponse(params.functionNumber);
}



/******* GetSegments ***********/
void requestGetSegments() {
      ws2812fx.stop();
    WS2812FX::segment _segments[ws2812fx.getNumSegments()];
    memcpy(_segments, ws2812fx.getSegments(), sizeof(_segments));
    packetSerial.send((const byte *)&_segments, sizeof(_segments));
    ws2812fx.start();
}


/******* ResetSegments ***********/

struct ResetSegmentsParams {
  uint8_t functionNumber;
  uint8_t numSegments;
  WS2812FX::Segment segments[MAX_NUM_SEGMENTS];
};

void requestResetSegments(byte* requestBytes) {
  ResetSegmentsParams params;
    memcpy(&params, requestBytes, sizeof(params));
    if(params.numSegments > MAX_NUM_SEGMENTS) {
      sendDefaultResponse(ERROR_RESPONSE);
    } else {

  /*
   * not needed yet because length wouldn't change
   * ws2812fx.setLength(LED_COUNT);
   * ws2812fx.stop(); // reset strip again in case length was increased
   */
    ws2812fx.clear();
    ws2812fx.resetSegments();
    ws2812fx.setNumSegments(params.numSegments); // reset number of segments
    memcpy( ws2812fx.getSegments(),params.segments, sizeof(params.segments));
    sendDefaultResponse(params.functionNumber);

    }
}


/******* UpdateSegmentConfigAll ***********/

struct UpdateSegmentConfigAllParams {
  uint8_t functionNumber;
  SegmentConfig config;
};


void requestUpdateSegmentConfigAll(byte* requestBytes) {
    UpdateSegmentConfigAllParams params;
    memcpy(&params, requestBytes, sizeof(params));
    //get current segment status
    WS2812FX::segment* segments = ws2812fx.getSegments();
    for(int i = 0; i < ws2812fx.getNumSegments(); i++)
    {
      segments[i].mode = params.config._mode;
      segments[i].colors[0] = params.config._colors[0];
      segments[i].colors[1] = params.config._colors[1];
      segments[i].colors[2] = params.config._colors[2];
      segments[i].speed = params.config._speed;
      segments[i].options = params.config._options;
    }
    sendDefaultResponse(params.functionNumber);
}

/******* UpdateSegmentConfigMulti ***********/

struct UpdateSegmentConfigMultiParams {
  uint8_t functionNumber;
  SegmentConfig config;
  uint8_t numSegments;
  uint8_t segmentIndices[MAX_NUM_SEGMENTS];
};

void requestUpdateSegmentConfigMulti(byte* requestBytes) {
  UpdateSegmentConfigMultiParams params;
  memcpy(&params, requestBytes, sizeof(params));

  WS2812FX::segment* segments = ws2812fx.getSegments();
  for(int i = 0; i < params.numSegments; i++)
  {
      segments[params.segmentIndices[i]].mode = params.config._mode;
      segments[params.segmentIndices[i]].colors[0] = params.config._colors[0];
      segments[params.segmentIndices[i]].colors[1] = params.config._colors[1];
      segments[params.segmentIndices[i]].colors[2] = params.config._colors[2];
      segments[params.segmentIndices[i]].speed = params.config._speed;
      segments[params.segmentIndices[i]].options = params.config._options;
  }
  sendDefaultResponse(params.functionNumber);
}
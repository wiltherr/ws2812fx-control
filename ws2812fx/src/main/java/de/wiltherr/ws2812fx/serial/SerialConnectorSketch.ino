
//Paket serial
#include <PacketSerial.h>
PacketSerial packetSerial;

//WS2812
#include <WS2812FX.h>
#define LED_PIN   6 // digital pin used to drive the LED strip (esp8266)
#define LED_COUNT 144 // number of LEDs on the strip
WS2812FX ws2812fx = WS2812FX(LED_COUNT, LED_PIN, NEO_GRBW + NEO_KHZ800);

/*
//NeoPixelBus
//#include <NeoPixelBus.h>
//NeoPixelBus<NeoGrbwFeature, Neo800KbpsMethod> strip(LED_COUNT, LED_PIN);
//NeoPixelBus<NeoGrbwFeature, NeoWs2812Method > strip(LED_COUNT, LED_PIN);

void myCustomShow(void) {
    // copy the ws2812fx pixel data to the dma pixel data
    memcpy(strip.Pixels(), ws2812fx.getPixels(), strip.PixelsSize());
    strip.Dirty();
    if(strip.CanShow() && Serial.available() <= 0) {
      ws2812fx.pause();
      strip.Show();
      ws2812fx.resume();
    }
}*/


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

#define STATE_STOP_REQUEST 0
#define STATE_START_REQUEST 1
#define STATE_PAUSE_REQUEST 2
#define STATE_RESUME_REQUEST 3
struct SetStateRequest {
  uint8_t functionNumber;
  uint8_t state;
};


struct SegmentConfig {
  uint16_t _speed;
  uint8_t _mode;
  uint8_t _options;
  uint32_t _colors[NUM_COLORS];
};

struct GetSegmentRequest {
  uint8_t functionNumber;
  uint8_t segmentIndex;
};

struct PixelColorPair {
  uint16_t pixelIndex;
  uint32_t color;
};

struct SetPixelColorRequest {
  uint8_t functionNumber;
  PixelColorPair pair;
};

#define MAX_MULTI_PIXEL_COLOR_PAIRS 30 //= ??? bytes per request TODO
struct SetMultiPixelColorRequest {
  uint8_t functionNumber;
  uint8_t pairCount;
  PixelColorPair pairs[MAX_MULTI_PIXEL_COLOR_PAIRS];
};





struct SetSegmentRequest {
  uint8_t functionNumber;
  uint8_t segmentIndex;
  SegmentConfig config;
};

struct ResetSegmentsRequest {
  uint8_t functionNumber;
  uint8_t numSegments;
  WS2812FX::Segment _segments[10];
};

struct SetAllSegmentsRequest {
  uint8_t functionNumber;
  SegmentConfig config;
};


struct Test {
  uint32_t a;
  uint32_t b;
};



void setup() {
  // init packet serial
  packetSerial.begin(2400);
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
  ws2812fx.setSegment(0,     ((LED_COUNT/4)*0), ((LED_COUNT/4)*1)-1, FX_MODE_STATIC, (const uint32_t[]) {0xFF000000,0x000000FF,BLACK},  1000, false);
  ws2812fx.setSegment(1,     ((LED_COUNT/4)*1), ((LED_COUNT/4)*2)-1, FX_MODE_STATIC, (const uint32_t[]) {RED,0x000000FF,BLACK},  1000, true);
  ws2812fx.setSegment(2,     ((LED_COUNT/4)*2), ((LED_COUNT/4)*3)-1, FX_MODE_STATIC, (const uint32_t[]) {BLUE,0x000000FF,BLACK},  1000, false);
  ws2812fx.setSegment(3,     ((LED_COUNT/4)*3), LED_COUNT-1        , FX_MODE_STATIC, (const uint32_t[]) {ORANGE,0x000000FF,BLACK},  1000, true);
  ws2812fx.start();
}

void loop() {

  packetSerial.update();
   ws2812fx.service();

}

void onPacketReceived(const uint8_t* buffer, size_t size)
{
  // In this example, we will simply reverse the contents of the array and send
  // it back to the sender.

   // Make a temporary buffer.

  byte inBuffer[size];

  // Copy the packet into our temporary buffer.
  memcpy(inBuffer, buffer, size);
  if(inBuffer[0] == 0x01) { //1 = SetState
    SetStateRequest request;
    memcpy(&request, inBuffer, sizeof(request));
    if(request.state == STATE_STOP_REQUEST) {
      ws2812fx.stop();
    } else if(request.state == STATE_START_REQUEST) {
      ws2812fx.start();
    } else if(request.state == STATE_PAUSE_REQUEST) {
      ws2812fx.pause();
    } else if(request.state == STATE_RESUME_REQUEST) {
      ws2812fx.resume();
    } else {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }
    sendDefaultResponse(request.functionNumber);
  } else if(inBuffer[0] == 0x02) { //2 = SetSegmentRequest
    SetSegmentRequest request;
    memcpy(&request, inBuffer, sizeof(request));

    if(request.segmentIndex > (ws2812fx.getNumSegments()-1)) {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }

    WS2812FX::segment* segments = ws2812fx.getSegments();
    segments[request.segmentIndex].mode = request.config._mode;
    segments[request.segmentIndex].colors[0] = request.config._colors[0];
    segments[request.segmentIndex].colors[1] = request.config._colors[1];
    segments[request.segmentIndex].colors[2] = request.config._colors[2];
    segments[request.segmentIndex].speed = request.config._speed;
    segments[request.segmentIndex].options = request.config._options;

    sendDefaultResponse(request.functionNumber);
  } else if(inBuffer[0] == 0x03) { //3 = SetAllSegments
    SetAllSegmentsRequest request;
    memcpy(&request, inBuffer, sizeof(request));
    //get current segment status
    WS2812FX::segment* segments = ws2812fx.getSegments();
    for(int i = 0; i < ws2812fx.getNumSegments(); i++)
    {
      segments[i].mode = request.config._mode;
      segments[i].colors[0] = request.config._colors[0];
      segments[i].colors[1] = request.config._colors[1];
      segments[i].colors[2] = request.config._colors[2];
      segments[i].speed = request.config._speed;
      segments[i].options = request.config._options;
    }
    sendDefaultResponse(request.functionNumber);
  } else if(inBuffer[0] == 0x04) { //4 = ResetSegments
     ResetSegmentsRequest request;
    memcpy(&request, inBuffer, sizeof(request));

    if(request.numSegments > MAX_NUM_SEGMENTS) {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }

  /*
   * not needed because we wont change Length
   * ws2812fx.setLength(LED_COUNT);
   * ws2812fx.stop(); // reset strip again in case length was increased
   */
  //ws2812fx.resetSegments();
    ws2812fx.setNumSegments(request.numSegments); // reset number of segments
    memcpy( ws2812fx.getSegments(),request._segments, sizeof(request._segments));
    sendDefaultResponse(request.functionNumber);
  } else if(inBuffer[0] == 0x05) { //5 = GetSegments
    ws2812fx.stop();
    WS2812FX::segment _segments[ws2812fx.getNumSegments()];
    memcpy(_segments, ws2812fx.getSegments(), sizeof(_segments));
    packetSerial.send((const byte *)&_segments, sizeof(_segments));
    ws2812fx.start();
   } else if(inBuffer[0] == 0x06) { //6 = GetSegment
    ws2812fx.stop();
    GetSegmentRequest request;
    memcpy(&request, inBuffer, sizeof(request));
    if(request.segmentIndex > (ws2812fx.getNumSegments()-1)) {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }
    WS2812FX::segment* segments = ws2812fx.getSegments();
    WS2812FX::segment _segment = segments[request.segmentIndex];
    packetSerial.send((const byte *)&_segment, sizeof(_segment));
    ws2812fx.start();
  }  else if (inBuffer[0] == 0x07) { //7 = SetPixelColor
    SetPixelColorRequest request;
    memcpy(&request, inBuffer, sizeof(request));
    if(request.pair.pixelIndex > (ws2812fx.getLength()-1)) {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }
    ws2812fx.setPixelColor(request.pair.pixelIndex, request.pair.color);
    ws2812fx.show();
    sendDefaultResponse(request.functionNumber);
  }  else if (inBuffer[0] == 0x08) { //8 = SetMultiPixelColor
    SetMultiPixelColorRequest request;
    memcpy(&request, inBuffer, sizeof(request));

    for(int i = 0; i < request.pairCount; i++) {
      if(request.pairs[i].pixelIndex > (ws2812fx.getLength()-1)) {
        sendDefaultResponse(ERROR_RESPONSE);
        return;
      }
      ws2812fx.setPixelColor(request.pairs[i].pixelIndex, request.pairs[i].color);
      ws2812fx.show();
    }
    sendDefaultResponse(request.functionNumber);
  } else {
    sendDefaultResponse(FUNCTION_NOT_FOUND_RESPONSE);
  }
}

void sendDefaultResponse(uint8_t _functionNumber) {
    DefaultResponse response;
    response.functionNumber = _functionNumber;
    packetSerial.send((const byte *)&response, sizeof(response));
}

void reverse(uint8_t* buffer, size_t size)
{
  uint8_t tmp;

  for (size_t i = 0; i < size / 2; i++)
  {
    tmp = buffer[i];
    buffer[i] = buffer[size - i - 1];
    buffer[size - i - 1] = tmp;
  }
}
//Paket serial
#include <PacketSerial.h>
PacketSerial packetSerial;

//WS2812
#include <WS2812FX.h>
#define LED_PIN   6 // digital pin used to drive the LED strip (esp8266)
#define LED_COUNT 144 // number of LEDs on the strip
WS2812FX ws2812fx = WS2812FX(LED_COUNT, LED_PIN, NEO_GRBW + NEO_KHZ800);

/*
//NeoPixelBus
//#include <NeoPixelBus.h>
//NeoPixelBus<NeoGrbwFeature, Neo800KbpsMethod> strip(LED_COUNT, LED_PIN);
//NeoPixelBus<NeoGrbwFeature, NeoWs2812Method > strip(LED_COUNT, LED_PIN);

void myCustomShow(void) {
    // copy the ws2812fx pixel data to the dma pixel data
    memcpy(strip.Pixels(), ws2812fx.getPixels(), strip.PixelsSize());
    strip.Dirty();
    if(strip.CanShow() && Serial.available() <= 0) {
      ws2812fx.pause();
      strip.Show();
      ws2812fx.resume();
    }
}*/


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

#define STATE_STOP_REQUEST 0
#define STATE_START_REQUEST 1
#define STATE_PAUSE_REQUEST 2
#define STATE_RESUME_REQUEST 3
struct SetStateRequest {
  uint8_t functionNumber;
  uint8_t state;
};


struct SegmentConfig {
  uint16_t _speed;
  uint8_t _mode;
  uint8_t _options;
  uint32_t _colors[NUM_COLORS];
};

struct GetSegmentRequest {
  uint8_t functionNumber;
  uint8_t segmentIndex;
};

struct PixelColorPair {
  uint16_t pixelIndex;
  uint32_t color;
};

struct SetPixelColorRequest {
  uint8_t functionNumber;
  PixelColorPair pair;
};

#define MAX_MULTI_PIXEL_COLOR_PAIRS 30 //= ??? bytes per request TODO
struct SetMultiPixelColorRequest {
  uint8_t functionNumber;
  uint8_t pairCount;
  PixelColorPair pairs[MAX_MULTI_PIXEL_COLOR_PAIRS];
};





struct SetSegmentRequest {
  uint8_t functionNumber;
  uint8_t segmentIndex;
  SegmentConfig config;
};

struct ResetSegmentsRequest {
  uint8_t functionNumber;
  uint8_t numSegments;
  WS2812FX::Segment _segments[10];
};

struct SetAllSegmentsRequest {
  uint8_t functionNumber;
  SegmentConfig config;
};


struct Test {
  uint32_t a;
  uint32_t b;
};



void setup() {
  // init packet serial
  packetSerial.begin(2400);
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
  ws2812fx.setSegment(0,     ((LED_COUNT/4)*0), ((LED_COUNT/4)*1)-1, FX_MODE_STATIC, (const uint32_t[]) {0xFF000000,0x000000FF,BLACK},  1000, false);
  ws2812fx.setSegment(1,     ((LED_COUNT/4)*1), ((LED_COUNT/4)*2)-1, FX_MODE_STATIC, (const uint32_t[]) {RED,0x000000FF,BLACK},  1000, true);
  ws2812fx.setSegment(2,     ((LED_COUNT/4)*2), ((LED_COUNT/4)*3)-1, FX_MODE_STATIC, (const uint32_t[]) {BLUE,0x000000FF,BLACK},  1000, false);
  ws2812fx.setSegment(3,     ((LED_COUNT/4)*3), LED_COUNT-1        , FX_MODE_STATIC, (const uint32_t[]) {ORANGE,0x000000FF,BLACK},  1000, true);
  ws2812fx.start();
}

void loop() {

  packetSerial.update();
   ws2812fx.service();

}

void onPacketReceived(const uint8_t* buffer, size_t size)
{
  // In this example, we will simply reverse the contents of the array and send
  // it back to the sender.

   // Make a temporary buffer.

  byte inBuffer[size];

  // Copy the packet into our temporary buffer.
  memcpy(inBuffer, buffer, size);
  if(inBuffer[0] == 0x01) { //1 = SetState
    SetStateRequest request;
    memcpy(&request, inBuffer, sizeof(request));
    if(request.state == STATE_STOP_REQUEST) {
      ws2812fx.stop();
    } else if(request.state == STATE_START_REQUEST) {
      ws2812fx.start();
    } else if(request.state == STATE_PAUSE_REQUEST) {
      ws2812fx.pause();
    } else if(request.state == STATE_RESUME_REQUEST) {
      ws2812fx.resume();
    } else {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }
    sendDefaultResponse(request.functionNumber);
  } else if(inBuffer[0] == 0x02) { //2 = SetSegmentRequest
    SetSegmentRequest request;
    memcpy(&request, inBuffer, sizeof(request));

    if(request.segmentIndex > (ws2812fx.getNumSegments()-1)) {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }

    WS2812FX::segment* segments = ws2812fx.getSegments();
    segments[request.segmentIndex].mode = request.config._mode;
    segments[request.segmentIndex].colors[0] = request.config._colors[0];
    segments[request.segmentIndex].colors[1] = request.config._colors[1];
    segments[request.segmentIndex].colors[2] = request.config._colors[2];
    segments[request.segmentIndex].speed = request.config._speed;
    segments[request.segmentIndex].options = request.config._options;

    sendDefaultResponse(request.functionNumber);
  } else if(inBuffer[0] == 0x03) { //3 = SetAllSegments
    SetAllSegmentsRequest request;
    memcpy(&request, inBuffer, sizeof(request));
    //get current segment status
    WS2812FX::segment* segments = ws2812fx.getSegments();
    for(int i = 0; i < ws2812fx.getNumSegments(); i++)
    {
      segments[i].mode = request.config._mode;
      segments[i].colors[0] = request.config._colors[0];
      segments[i].colors[1] = request.config._colors[1];
      segments[i].colors[2] = request.config._colors[2];
      segments[i].speed = request.config._speed;
      segments[i].options = request.config._options;
    }
    sendDefaultResponse(request.functionNumber);
  } else if(inBuffer[0] == 0x04) { //4 = ResetSegments
     ResetSegmentsRequest request;
    memcpy(&request, inBuffer, sizeof(request));

    if(request.numSegments > MAX_NUM_SEGMENTS) {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }

  /*
   * not needed because we wont change Length
   * ws2812fx.setLength(LED_COUNT);
   * ws2812fx.stop(); // reset strip again in case length was increased
   */
  //ws2812fx.resetSegments();
    ws2812fx.setNumSegments(request.numSegments); // reset number of segments
    memcpy( ws2812fx.getSegments(),request._segments, sizeof(request._segments));
    sendDefaultResponse(request.functionNumber);
  } else if(inBuffer[0] == 0x05) { //5 = GetSegments
    ws2812fx.stop();
    WS2812FX::segment _segments[ws2812fx.getNumSegments()];
    memcpy(_segments, ws2812fx.getSegments(), sizeof(_segments));
    packetSerial.send((const byte *)&_segments, sizeof(_segments));
    ws2812fx.start();
   } else if(inBuffer[0] == 0x06) { //6 = GetSegment
    ws2812fx.stop();
    GetSegmentRequest request;
    memcpy(&request, inBuffer, sizeof(request));
    if(request.segmentIndex > (ws2812fx.getNumSegments()-1)) {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }
    WS2812FX::segment* segments = ws2812fx.getSegments();
    WS2812FX::segment _segment = segments[request.segmentIndex];
    packetSerial.send((const byte *)&_segment, sizeof(_segment));
    ws2812fx.start();
  }  else if (inBuffer[0] == 0x07) { //7 = SetPixelColor
    SetPixelColorRequest request;
    memcpy(&request, inBuffer, sizeof(request));
    if(request.pair.pixelIndex > (ws2812fx.getLength()-1)) {
      sendDefaultResponse(ERROR_RESPONSE);
      return;
    }
    ws2812fx.setPixelColor(request.pair.pixelIndex, request.pair.color);
    ws2812fx.show();
    sendDefaultResponse(request.functionNumber);
  }  else if (inBuffer[0] == 0x08) { //8 = SetMultiPixelColor
    SetMultiPixelColorRequest request;
    memcpy(&request, inBuffer, sizeof(request));

    for(int i = 0; i < request.pairCount; i++) {
      if(request.pairs[i].pixelIndex > (ws2812fx.getLength()-1)) {
        sendDefaultResponse(ERROR_RESPONSE);
        return;
      }
      ws2812fx.setPixelColor(request.pairs[i].pixelIndex, request.pairs[i].color);
      ws2812fx.show();
    }
    sendDefaultResponse(request.functionNumber);
  } else {
    sendDefaultResponse(FUNCTION_NOT_FOUND_RESPONSE);
  }
}

void sendDefaultResponse(uint8_t _functionNumber) {
    DefaultResponse response;
    response.functionNumber = _functionNumber;
    packetSerial.send((const byte *)&response, sizeof(response));
}

void reverse(uint8_t* buffer, size_t size)
{
  uint8_t tmp;

  for (size_t i = 0; i < size / 2; i++)
  {
    tmp = buffer[i];
    buffer[i] = buffer[size - i - 1];
    buffer[size - i - 1] = tmp;
  }
}
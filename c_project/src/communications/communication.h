#pragma once

#include <libpropeller/serial/serial.h>
#include <stdint.h>
#include "../utils/data/vars/vars.h"
#include "../utils/util.h"
#include "../utils/data/pins/pins.h"
#include "ping.h"
#include "atomic"
#include "../utils/data/runnables/cogs.h"


/**
 *
 * Cog for Communicating with the server implementing the same protocol
 * To not start an extra COG it also fires the ping sensor at the front (could be improved)
 *
 *
 * Protocol:
 *
 * Byte:
 * +---------+
 * |         |
 * +---------+
 *
 * Multiple Bytes:
 * +=========+
 * |         |
 * +=========+
 *
 *
 *
 * Frame:
 * +---------++---------++---------++---------++---------++---------++=========++---------+
 * |  0x6E   ||  XXXX   ||  XXXX   ||  XXXX   ||  XXXX   ||  YYYY   ||  DATA   ||  0x7F   |
 * +---------++---------++---------++---------++---------++---------++=========++---------+
 *
 * XXXX-XXXX-XXXX-XXXX: Length of the msgpack object _without_ START and END byte and without ID
 * YYYY: Object ID
 *
 */
class Communication {
public:
    //Constants
    static const unsigned char END_BYTE = 0x7F;
    static const unsigned char START_BYTE = 0x6E;
    static const int BUFFER_SIZE = 64;
    static const int CHUNK_SIZE = 16;
    static const int DEBUG_LEVEL = 1;
    static const int WIFI_RX_PIN = 9;
    static const int WIFI_TX_PIN = 8;

    static int init();
    static void send(const unsigned char *buffer, int bufferlength, unsigned char topic);
    static std::atomic_flag roundaboutBufferLock;

private:
    static libpropeller::Serial wifi;
    static unsigned char buffer[BUFFER_SIZE];

    //static const char CMDNU[3]; // = {9, 1, '\n'};
    static bool isRunning;
    static std::atomic_flag sendLock;

    static void handleBuffer(int length, int typeID);

};

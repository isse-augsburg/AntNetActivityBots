#include <PropWare/hmi/output/synchronousprinter.h>
#include "communication.h"


int Communication::init() {
    pwSyncOut.printf("Running communication in cog: %i\n", cogid());

    sendLock.clear();
    isRunning = true;
    wifi = libpropeller::Serial();
    pwSyncOut.printf("Started wifi: %i\n", wifi.Start(WIFI_RX_PIN, WIFI_TX_PIN, 115200));
    libpropeller::Stopwatch stopwatch = libpropeller::Stopwatch();
    stopwatch.Start();

    int length = 0;
    int typeID = 0;
    int bytesRead = 0;

    /*
     * run
     */

    while (isRunning) {
        // pwSyncOut.printf("count: %i\n", wifi.GetCount());
        if (length > 0) {
            // + 2 for end byte and start byte
            // + 1 for type
            // + 4 for size
            if ((length + 7) > bytesRead && wifi.GetCount() < min((length + 7) - bytesRead, CHUNK_SIZE))
                continue;

            while ((length + 7) > bytesRead && wifi.GetCount() >= min((length + 7) - bytesRead, CHUNK_SIZE))
                bytesRead += wifi.Get((char *) (buffer + bytesRead), min((length + 7) - bytesRead, CHUNK_SIZE));

            if ((length + 7) > bytesRead)
                continue;

            if (buffer[length + 6] == END_BYTE) { //Command valid
                // pwSyncOut.printf("Lenght: %i Type: %i\n", length, typeID);
                handleBuffer(length, typeID);

            } else { // didn't receive end byte
                pwSyncOut.printf("ERROR: Lenght: %i Type: %i\n", length, typeID);
                int c;
                while (((c = wifi.Get()) != END_BYTE) && c != -1);
            }

            length = -2;
        } else {
            if (wifi.GetCount() >= 6) {
                buffer[0] = static_cast<unsigned char>(wifi.Get());
                if (buffer[0] == START_BYTE) {

                    buffer[1] = static_cast<unsigned char>(wifi.Get());
                    buffer[2] = static_cast<unsigned char>(wifi.Get());
                    buffer[3] = static_cast<unsigned char>(wifi.Get());
                    buffer[4] = static_cast<unsigned char>(wifi.Get());

                    buffer[5] = static_cast<unsigned char>(wifi.Get());

                    length = getInteger(buffer, 1);
                    typeID = buffer[5];
                    bytesRead = 6;

                    if (length > BUFFER_SIZE) {
                        pwSyncOut.printf("Received object too large: %i", length);
                        length = -1;
                    }
                }
            }
        }

        if (stopwatch.GetElapsed() > 1000 + (rand() % 100)) {
            VARS::pingCM = ping_cm(PING_PIN);
            stopwatch.Start();
        }
    }

    return 0;
}

void Communication::handleBuffer(int length, int typeID) {
    pwSyncOut.printf("Got type: %i with length: %i\n", typeID, length);

    switch (typeID) {
        case 2:
            while (roundaboutBufferLock.test_and_set(std::memory_order_acquire)) {
                pause(1);
            }
            Cogs::lineFollower.exitToChoose = (signed char) buffer[6];
            Cogs::lineFollower.errorValue = buffer[7];
            pwSyncOut.printf("Exit number: %i\n", buffer[6]);

            Communication::roundaboutBufferLock.clear(std::memory_order_release);


            //send({0}, 1, 1);
            break;
        case 10:
            pwSyncOut.printf("Got a ping request\n");
            unsigned char buf[] = {0};
            send(buf, 1, 11);

            auto should = false;
            auto code = -1;

            while (Communication::roundaboutBufferLock.test_and_set(std::memory_order_acquire)) {
                pause(1);
            }

            if (Cogs::lineFollower.roundaboutState == LineFollower::INSIDE && Cogs::lineFollower.exitToChoose == -1) {
                should = true;
                code = Cogs::lineFollower.roundaboutBuffer;
            }
            Communication::roundaboutBufferLock.clear(std::memory_order_release);

            if (should) {
                auto *code_buf = (unsigned char *) &code;
                Communication::send(code_buf, 1, 1);
            }

            break;
    }
}


void Communication::send(const unsigned char *buffer, int bufferlength, unsigned char type) {
    while (sendLock.test_and_set(std::memory_order_acquire)) {
        pause(1);
        // pwSyncOut.printf("locked\n");
    }

    pwSyncOut.printf("SENDED: %i\n", buffer[0]);

    wifi.Put(START_BYTE);
    wifi.Put((bufferlength >> 24u) & 0xFFu);
    wifi.Put((bufferlength >> 16u) & 0xFFu);
    wifi.Put((bufferlength >> 8u) & 0xFFu);
    wifi.Put(bufferlength & 0xFF);
    wifi.Put(type);
    wifi.Put((char *) buffer, bufferlength);
    wifi.Put(END_BYTE);

    sendLock.clear(std::memory_order_release);
}

//private:
// ----------------------------------- Variables ------------------------------------------
libpropeller::Serial Communication::wifi;
unsigned char Communication::buffer[BUFFER_SIZE];

bool Communication::isRunning;
std::atomic_flag Communication::sendLock = ATOMIC_FLAG_INIT;
std::atomic_flag Communication::roundaboutBufferLock = ATOMIC_FLAG_INIT;


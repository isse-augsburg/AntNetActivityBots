#pragma once


#include "../../utils/data/vars/vars.h"
#include "../../utils/util.h"
#include "PropWare/concurrent/runnable.h"
#include "../../../drive/abdrive.h"
#include "PropWare/hmi/output/synchronousprinter.h"
#include "../../utils/data/pins/pins.h"
#include <libpropeller/stopwatch/stopwatch.h>
#include <atomic>

/**
 * min distance in cm of the ping sensor before stopping driving
 */
#define MIN_DISTANCE 12

/**
 * Line Follower Runnable manages staying on the lines and reading the barcodes
 */
class LineFollower : public PropWare::Runnable {
    /**
     * State of the LineFollowers reading the barcode
     * Changes to reading when finding a Black line with the center Sensors marking the start of every barcode
     */
    enum LineFollowerState {
        AWAITING_BLACK_LINE, READING
    };

public:
    /**
     * Where the Bot is currently in relation to a roundabout
     */
    enum RoundaboutState {
        INSIDE, OUTSIDE, AWAITING_CORNER, ERROR
    };

    /**
     * The speed the Bot drives with
     * Anything above 20 causes problems with the speed of the reading of the Barcodes

     * The controller is optimized for 20 and might cause problems when faster
     */
    static const int DEFAULT_SPEED = 20;

    /**
     * Configuration value for the PID Controller
     */
    static const float KP; // value in CPP file

    /**
     * Configuration value for the PID Controller
     */
    static const int KID = 20;

    template<size_t N>
    explicit // OUTSIDE
    LineFollower(const uint32_t (&stack)[N]) : Runnable(stack), isRunning(true), roundaboutState(INSIDE), data(0),
                                               callbackTime(-1), callbackTimer(libpropeller::Stopwatch()),
                                               exitToChoose(-1), currentExitCount(0), errorValue(0) {
    }

    virtual void run();

    signed char convertValue(unsigned char data);
    volatile signed char exitToChoose;
    volatile unsigned char errorValue;
    volatile unsigned char roundaboutBuffer;
    volatile RoundaboutState roundaboutState;


private:
    volatile bool isRunning;
    unsigned char data;
    libpropeller::Stopwatch callbackTimer;
    int callbackTime;

    bool barcodeCallback(unsigned int code, unsigned int length);
    bool blackLineInsideCallback();

    bool timeCallback();

    unsigned char currentExitCount;
};
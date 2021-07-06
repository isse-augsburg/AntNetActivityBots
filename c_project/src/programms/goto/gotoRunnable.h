#pragma once

#include "PropWare/concurrent/runnable.h"
#include "../../../drive/abdrive.h"
#include "../../utils/util.h"

/**
 * Cogs/Thread to manage the speed of the bot without having to set the value all the time.
 *
 * Could probably be removed as it was only necessary early in the development
 */
class GotoRunnable : public PropWare::Runnable {

public:
    volatile bool isRunning, shouldDrive;

    template<size_t N>
    explicit GotoRunnable(const uint32_t (&stack)[N]) : Runnable(stack), isRunning(true), valueLeft(0), valueRight(0), shouldDrive(true) {
    }

    virtual void run();

    /**
     * Sets the current speed of the bot
     * @param l
     * @param r
     */
    void setSpeed(int l, int r);

private:
    volatile int valueLeft, valueRight;

};

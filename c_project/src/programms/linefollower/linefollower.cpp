#include <Arduino.h>
#include "linefollower.h"
#include "../../utils/data/runnables/cogs.h"
#include "ping.h"
#include "../../communications/communication.h"
#include "PauseTimer.h"

// ---------------------- CONSTS ------------------------------
const float LineFollower::KP = 0.2f; //0.1f;

/**
 * Drives as long as there is room in the front
 */
void tryDriveSpeed(int l, int r) {
    if (VARS::pingCM > MIN_DISTANCE) {
        Cogs::gotoRunnable.setSpeed(l, r);
    } else {
        Cogs::gotoRunnable.setSpeed(0, 0);
    }
}

/**
 * data pin layout:
 * 0b110000000
 *   |||||||||
 *   012345678
 *
 *
 * Starts the cog and starts the loop where everything is happening
 */
void LineFollower::run() {
    pwSyncOut.printf("Line loop started: %i\n", cogid());
    pause(1000);

    set_directions(7, 0, 0);

    float integral = 0;
    unsigned char middle = 0;
    unsigned char rightEncoder = 0;
    unsigned char encoderBitChanged = 0;
    // Ton of stopwatches that manage pauses from having another bot infront of it
    PauseTimer stopwatch = PauseTimer();
    PauseTimer whiteStopwatch = PauseTimer();
    PauseTimer global = PauseTimer();
    PauseTimer runtime = PauseTimer();
    PauseTimer offRoadTimer = PauseTimer();

    global.Start();
    runtime.Start();
    int last = global.GetElapsed();
    int barcode = 0;
    int lines = 0;
    LineFollowerState state = AWAITING_BLACK_LINE;
    roundaboutState = OUTSIDE;
    int speed = DEFAULT_SPEED;
    float dirL = 0, dirR = 0;
    float currentPos = 0;
    bool alreadyOnBlackInside = false;

    int pingFreeCount = 5;

    while (isRunning) {
        // Factor scales with times between the loops, to not depend on code execution speed
        float factor = runtime.GetElapsed() / 100.0;
        runtime.Start();

        // pause all timers when driving is blocked
        if (VARS::pingCM <= MIN_DISTANCE) {
            Cogs::gotoRunnable.setSpeed(0, 0);
            pingFreeCount = 5;

            stopwatch.Pause();
            whiteStopwatch.Pause();
            global.Pause();
            runtime.Pause();

            continue;
        } else {
            // wait few iterations before starting again
            if (pingFreeCount > 0) {
                pingFreeCount--;
                pause(20);
                continue;
            }

            stopwatch.Continue();
            whiteStopwatch.Continue();
            global.Continue();
            runtime.Continue();
        }

        // some code has started a callback for a given time that is now elapsed
        if (callbackTime > 0 && callbackTimer.GetStarted() && callbackTimer.GetElapsed() > callbackTime) {
            if (timeCallback()) {
                runtime.Start();
                integral = 0;
                dirL = 0;
                dirR = 0;
            }
        }

        // read ping sensors from pins 0 to 7
        data = (unsigned char) (get_states(7, 0));
        // int currentPos = 0 + ((data & 0b11) == 0b01) + ((data >> 6 & 0b11) == 0b01) + ((data & 0b11) == 0b10) * -1 + ((data >> 6 & 0b11) == 0b10) * -1;

        // check if on pure black or pure white (off road check)
        if (data == 0b11111111 || data == 0b00000000) {
            if (offRoadTimer.GetStarted()) {
                if (offRoadTimer.GetElapsed() > 3000) {
                    Cogs::gotoRunnable.shouldDrive = false;
                    drive_goto(1, 1);
                    pause(100);

                    while (true) {
                        low(27);
                        high(26);

                        pause(200);
                        low(26);
                        high(27);
                        pause(200);
                    }
                }
            } else {
                offRoadTimer.Start();
            }
        } else {
            offRoadTimer.Reset();
        }


        unsigned int tempR = data & 0b11u;
        unsigned int tempL = (data >> 6u) & 0b11u;

        // ### Code for staying inside the Black lines
        // Factors for how far the sensor is off the black lines
        switch (tempL) {
            case 0b01:
                dirL = 0.9;
                break;
            case 0b10:
                dirL = -0.7;
                break;
            case 0b11:
                dirL = 0;
                break;
            case 0b00:
                if (data & 0b00100000u) {
                    dirL = 2;
                }
                break;
        }

        switch (tempR) {
            case 0b01:
                dirR = 0.7;
                break;
            case 0b10:
                dirR = -0.9;
                break;
            case 0b11:
                dirR = 0;
                break;
            case 0b00:
                if (data & 0b00000100) {
                    dirL = -2;
                }
                break;
        }

        currentPos = dirL + dirR;


        // for (int i = 0; i <= 7; i++) {
        //     pwSyncOut.printf("%i ", (data >> i) & 1);
        // }
        // pwSyncOut.printf("pos: %i\r", currentPos);


        // PI Controller with integrator that is clamped to a specific value
        // Tested by experimentation, has room for improvement at higher speeds
        if (abs(currentPos) > 0) {
            if (abs(integral) < speed)
                integral += m_ceil(speed / 20.0) * factor * currentPos
                            // should decrease overswing
                            * (1 + (integral > 0 && currentPos < 0) || (integral < 0 && currentPos > 0) * 4);
        } else {
            if (abs(integral) > 0) {
                integral -= m_ceil(speed / 20.0) * factor * (integral > 0 ? 1 : -1) * KID;
            }
            if (abs(integral) < 5) {
                integral = 0;
            }
        }

        // calculate final speed for both wheels
        int corrSpeedL = static_cast<int>(speed + (speed * KP * currentPos + integral * (1 - KP) * 0.3));
        int corrSpeedR = static_cast<int>(speed - (speed * KP * currentPos + integral * (1 - KP) * 0.3));

        tryDriveSpeed(corrSpeedL, corrSpeedR);
        // pwSyncOut.printf("l: %i r: %i currentPos: %i tempL: %i, integral: %i, factor: %f\n", corrSpeedL, corrSpeedR,
        //                  currentPos, tempL, integral, factor);


        //region >>>>> Code for reading barcodes
        middle = static_cast<unsigned char>((data >> 2u) & 0b1111u);
        if (rightEncoder != (middle & 1u)) {
            rightEncoder = static_cast<unsigned char>(middle & 1u);
            encoderBitChanged = 1;
            // last = stopwatch.GetElapsed();
            stopwatch.Start();
        }

        /// Check whether there is white for 100 msecs already, it not, then it isn't a line end
        if (state == READING && whiteStopwatch.GetStarted()) {
            if (middle == 0) {
                if (whiteStopwatch.GetElapsed() > 100) {
                    state = AWAITING_BLACK_LINE;
                    whiteStopwatch.Reset();
                    pwSyncOut.printf("[outer]>> final barcode: %i, lines: %i\n", barcode, lines);
                    if (barcodeCallback(barcode, lines)) {
                        runtime.Start();
                        integral = 0;
                    }
                    lines = 0;
                }
            } else {
                whiteStopwatch.Reset();
            }
        }

        /// Check if there is a black line on the inside of the roundabout
        if (roundaboutState == RoundaboutState::INSIDE) {
            // if ((middle & 0b0110) == 0b0110 && !alreadyOnBlackInside) {
            if ((middle & 0b0110u) && !alreadyOnBlackInside) { // TODO: Test if it fails
                alreadyOnBlackInside = true;
            }
            if (middle == 0b0000 && alreadyOnBlackInside) {
                alreadyOnBlackInside = false;
                blackLineInsideCallback();
            }
        } else {
            /// otherwise it tries to read a barcode
            if (encoderBitChanged && stopwatch.GetElapsed() > 20) {
                // last = stopwatch.GetElapsed();
                pwSyncOut.printf("middle bits: ");
                for (int i = 3; i >= 0; i--) {
                    pwSyncOut.printf("%i ", (middle >> i) & 1u);
                }
                signed char value = convertValue(middle);
                // int corr = !((middle >> 1 & 1) ^ (middle >> 2 & 1));
                int delta = global.GetElapsed() - last;
                pwSyncOut.printf(", number: %i, delta: %i\n", value, delta);
                last = global.GetElapsed();

                if (delta > 700) {
                    state = AWAITING_BLACK_LINE;
                    whiteStopwatch.Reset();
                }

                if (value >= 0 || middle == 0) {
                    encoderBitChanged = 0;

                    if (state == AWAITING_BLACK_LINE) {
                        if (middle == 0b1111) {
                            barcode = 0;
                            lines = 0;
                            state = READING;
                            //speed = 10;
                        }
                    } else {
                        if (middle == 0) {
                            // Checks for how long there is white before marking the end of the barcode
                            if (!whiteStopwatch.GetStarted()) {
                                whiteStopwatch.Start();
                            }

                            /*if (whiteStopwatch.GetElapsed() > 100 && whiteStopwatch.GetStarted() && state == READING) {
                                whiteStopwatch.Reset();
                                state = AWAITING_BLACK_LINE;
                                //speed = DEFAULT_SPEED;
                                pwSyncOut.printf("[inner]>> final barcode: %i, lines: %i\n", barcode, lines);
                                if (barcodeCallback(barcode, lines)) {
                                    runtime.Start();
                                    integral = 0;
                                }
                            }*/
                        } else {
                            whiteStopwatch.Reset();
                            barcode <<= 2;
                            barcode |= value;
                            lines++;
                            pwSyncOut.printf("barcode: %i, lines: %i\n", barcode, lines);
                        }
                    }
                } else {
                    stopwatch.Start();
                }
            }
        }

        // if (encoderBitChanged && stopwatch.GetElapsed() > 150 / (speed / 10)) {
        //endregion

        /*if (encoderBitChanged && stopwatch.GetElapsed() > 300) {
            encoderBitChanged = 0;
            pwSyncOut.printf("1 sec is over\n");
        }*/

        pause(15);
    }
}

/**
 * Converts the bit mask into the the the decoded bit value
 * @param data bit mask of the scanned line (only right bits, as left bit is always alternating)
 */
signed char LineFollower::convertValue(unsigned char data) {
    switch (data >> 1u) {
        case 0b111:
            return 0;
        case 0b110:
            return 1;
        case 0b011:
            return 2;
        case 0b101:
            return 3;
        default:
            return -1;
    }
}

/**
 *
 * Called when exiting the roundabout or when in an error state and killing the bot
 *
 * @return return true if you want to reset the timer and integrator
 */
bool LineFollower::timeCallback() {
    if (roundaboutState == AWAITING_CORNER) {
        callbackTimer.Reset();
        callbackTime = -1;

        if (errorValue == 0) {
            Cogs::gotoRunnable.shouldDrive = false;
            pause(10);
            drive_goto(25, -26);
            pause(10);
            Cogs::gotoRunnable.shouldDrive = true;
            roundaboutState = OUTSIDE;
            low(27);
        } else {
            Cogs::gotoRunnable.shouldDrive = false;
            pause(10);
            drive_goto(25, -26);
            pause(10);
            drive_goto(80, 80);

            roundaboutState = ERROR;

            while (true) {
                low(27);
                high(26);

                pause(100);
                low(26);
                high(27);
                pause(100);
            }
        }
    }
}

/**
 * Called when a black line is found inside a roundabout
 * Starts counting the current exit number till correct destination is reached
 */
bool LineFollower::blackLineInsideCallback() {
    currentExitCount = (currentExitCount + 1) % 4;
    if (exitToChoose == currentExitCount) {
        roundaboutState = AWAITING_CORNER;
        callbackTime = 575;
        callbackTimer.Start();

        return false;
    }
}

/**
 *
 * Called when a barcode is read
 *
 * @param code bit mask that have been read
 * @param length amount of vertical lines that have been read
 * @return return true if you want to reset the timer and integrator
 */
bool LineFollower::barcodeCallback(unsigned int code, unsigned int length) {

    // test code
    if (length == 2 && code == 1) {
        pause(800);
        Cogs::gotoRunnable.shouldDrive = false;
        pause(10);
        drive_goto(25, -26);
        pause(500);
        Cogs::gotoRunnable.shouldDrive = true;
    }

    // len 0 means black line in roundabout
    if (length == 0) {
        if (roundaboutState == INSIDE) {
            currentExitCount = (currentExitCount + 1) % 4;
            if (exitToChoose == currentExitCount) {
                roundaboutState = AWAITING_CORNER;
                callbackTime = 800;
                callbackTimer.Start();

                return false;
            }
        }
    }

    // len 4 is a normal roundabout barcode
    if (length == 4) {
        unsigned int id = code >> 3u;
        unsigned int nr = (code >> 1u) & 0b11u;
        bool isExit = (code & 1u) == 0;
        auto *buf = (unsigned char *) &code;

        // locking due to multiple cogs
        while (Communication::roundaboutBufferLock.test_and_set(std::memory_order_acquire)) {
            pause(1);
        }
        exitToChoose = -1;
        errorValue = 0;

        Communication::roundaboutBufferLock.clear(std::memory_order_release);


        // exitToChoose = 0;
        Communication::send(buf, 1, 1);
        toggle(27);
        toggle(26);


        if (!isExit) {
            while (Communication::roundaboutBufferLock.test_and_set(std::memory_order_acquire)) {
                pause(1);
            }
            roundaboutState = INSIDE;
            roundaboutBuffer = code;
            currentExitCount = nr;

            Communication::roundaboutBufferLock.clear(std::memory_order_release);


            //high(27);
            //low(26);

            Cogs::gotoRunnable.shouldDrive = false;
            drive_goto(-5, -5);
            pause(10);

            bool leftNotFree = true;
            bool frontNotFree = true;

            while (leftNotFree || frontNotFree) {
                drive_goto(-11, 10);
                leftNotFree = VARS::pingCM <= 30;

                drive_goto(11, -10);
                frontNotFree = VARS::pingCM <= 20;
            }

            /*drive_goto(-11, 10);
            while (VARS::pingCM <= 30) { pause(1); }
            drive_goto(11, -10);
            while (VARS::pingCM <= 30) { pause(1); }*/

            drive_goto(45, 45);
            drive_goto(25, -26);

            pause(10);
            Cogs::gotoRunnable.shouldDrive = true;
            pwSyncOut.printf("id: %i, nr: %i, isExit: %i\n", id, nr, isExit);

            return true;
        } else {
            low(27);
            high(26);
        }

    }

    return false;
}
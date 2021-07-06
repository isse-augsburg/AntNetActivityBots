#pragma once
#include <propeller.h>

/**
* Timer adapted from "libpropeller::Stopwatch" to support pausing the timer
 *
 * Warning from libpropeller::Stopwatch:
 * @warning The maximum time that can be recorded is (2^32/CLKFREQ) seconds. At
 * 80Mhz that is a bit over 53 seconds. Longer durations will rollover and make
 * the stopwatch operate incorrectly.
 *
 */
class PauseTimer {
public:
    PauseTimer() {
        // Optimization: doing this in a loop really slows things down. So
        // let's cut out the divide, and put in here in the constructor.
        kCyclesPerMillisecond = CLKFREQ / 1000;
        cumulative = 0;
        paused = false;
        Start(); //This call suppresses a warning.
        Reset();
    }

    /**
     * Stop timing.
     */
    void Reset() {
        started = false;
        cumulative = 0;
        paused = false;
    }

    /**
     * Start timing. Can be called without calling reset first.
     */
    void Start() {
        start_CNT_ = CNT;
        started = true;
    }

    /**
     * Get current stopwatch time (when started).
     *
     * @returns the number of elapsed milliseconds since start.
     */
    unsigned int GetElapsed() const {
        if (started) {
            return cumulative + (paused ? 0 : ((CNT - start_CNT_) / kCyclesPerMillisecond));
        } else {
            return 0;
        }
    }

    /**
     * Get started state.
     *
     * @return  true if started, false otherwise.
     */
    bool GetStarted() const {
        return started;
    }

    /**
     * Pauses the Timer, can be continued with [Continue]
     */
    void Pause() {
        cumulative = GetElapsed();
        paused = true;
    }

    /**
     * Continues the timer when in paused state
     */
    void Continue(){
        if (started && paused) {
            paused = false;
            start_CNT_ = CNT;
        }
    }

private:
    unsigned int start_CNT_;
    bool started;
    bool paused;
    unsigned int cumulative;
    unsigned int kCyclesPerMillisecond;

};

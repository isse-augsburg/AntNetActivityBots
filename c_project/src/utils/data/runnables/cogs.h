#pragma once

#include "propeller.h"
#include <stdint.h>
#include "../../../programms/linefollower/linefollower.h"
#include "../../../programms/goto/gotoRunnable.h"

#define COG_MEMORY_GOTO (100 + EXTRA_STACK_LONGS)
#define COG_MEMORY_LINEFOLLOWER (500 + EXTRA_STACK_LONGS)

/**
 * As the Propellor Processor supports up to 8 Cogs (Cores) they are used to sped up the processing
 * and run different activities simultaneously
 */
class Cogs {
public:
    /**
     * Starts all Runnables in new cogs
     *
     * Note, that there is still the main Cog which is part of the 8
     * (Communication is run on the main one here)
     */
    static void startAllCogs();

    /**
     * Memory Stack for the Linefollower COG
     * Needs quite a large stack to work
     */
    static uint32_t stack_line[COG_MEMORY_LINEFOLLOWER];

    /**
     * Memory Stack for the Movement COG
     */
    static uint32_t stack_goto[COG_MEMORY_GOTO];

    /**
     * LineFollower Runnable, can be accessed from a static context
     */
    static LineFollower lineFollower;

    /**
     * Goto (Movement) Runnable, can be accessed from a static context
     */
    static GotoRunnable gotoRunnable;
};
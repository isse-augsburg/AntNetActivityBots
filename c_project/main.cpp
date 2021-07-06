#include <propeller.h>
#include <simpletools.h>
#include <libpropeller/stopwatch/stopwatch.h>
#include "src/communications/communication.h"
#include "src/utils/data/runnables/cogs.h"


int main(void) {
    // drive_gotoMode(OFF);
    /* This is important to not lock up the linefollower thread (probably due to lack of memory or starting the cog)*/
    drive_speed(0,0);
    drive_setAcceleration(FOR_SPEED, 1200);
    // servo_speed(12, 0);
    // Test whether this is needed as well
    //drive_goto(0, 0);

    pwSyncOut.printf("Starting\n");


    Cogs::startAllCogs();
    Communication::init();


    while (1);
}
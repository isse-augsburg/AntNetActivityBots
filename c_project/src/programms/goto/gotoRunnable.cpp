#include <PropWare/hmi/output/synchronousprinter.h>
#include "gotoRunnable.h"
#include "../../communications/communication.h"


void GotoRunnable::run() {
    pause(50);
    pwSyncOut.printf("Started GotoRunnable in cog: %i\n", cogid());

    while (isRunning) {
        while(!shouldDrive) {pause(10);}
        // pwSyncOut.printf("speeds: %i, %i\n", valueLeft, valueRight);
        drive_speed(valueLeft, valueRight);
        pause(1);
    }
}

void GotoRunnable::setSpeed(int l, int r) {
    valueLeft = l;
    valueRight = r;
}

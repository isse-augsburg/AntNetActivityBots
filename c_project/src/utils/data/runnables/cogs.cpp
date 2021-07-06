#include "cogs.h"

uint32_t Cogs::stack_line[COG_MEMORY_LINEFOLLOWER];
uint32_t Cogs::stack_goto[COG_MEMORY_GOTO];
LineFollower Cogs::lineFollower(Cogs::stack_line);
GotoRunnable Cogs::gotoRunnable(Cogs::stack_goto);

void Cogs::startAllCogs() {
    pause(100);
    PropWare::Runnable::invoke(gotoRunnable);
    pause(100);
    PropWare::Runnable::invoke(lineFollower);
    pause(100);
}

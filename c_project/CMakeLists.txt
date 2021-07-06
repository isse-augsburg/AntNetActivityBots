cmake_minimum_required(VERSION 3.3)
find_package(PropWare REQUIRED)
project(AntNet CXX C)

set(MODEL cmm)
set(BOARD activityboard)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# SET(GCC_COVERAGE_COMPILE_FLAGS "-save-temps -mlmm -g -Os -mfcache -m32bit-doubles -fno-exceptions -fno-rtti -Dprintf=__simple_printf -o main.o -c main.cpp propeller-elf-gcc -save-temps -mlmm -g -Os -mfcache -m32bit-doubles -fno-exceptions -fno-rtti -Dprintf=__simple_printf -o c++-alloc.o -c c++-alloc.cpp propeller-elf-gcc -Xlinker -Map=main.rawmap -mlmm -o main.elf main.o c++-alloc.o propeller-elf-c++filt --strip-underscore < main.rawmap | perl propgcc-map-sizes.pl > main.map propeller-elf-objdump -d -S main.elf | propeller-elf-c++filt --strip-underscore > main.dump")

# Might be needed in the future
# -Wl,--gc-sections -Wl,--Map=out/mapfile.txt -fdata-sections -ffunction-sections

create_simple_executable(${PROJECT_NAME}
        main.cpp

        # ------------- UTIL -------------
        src/utils/util.cpp
        src/utils//c++-alloc.cpp
        src/utils/data/pins/pins.h
        src/utils/data/vars/vars.cpp
        src/utils/data/runnables/cogs.cpp

        # ------------- PROGRAMMS -------------
        src/programms/linefollower/linefollower.cpp
        src/programms/goto/gotoRunnable.cpp
        src/programms/linefollower/PauseTimer.h


        # ------------- COMMUNICATION -------------
        src/communications/communication.cpp


        # ------------- LIBRARY -------------
        # This had to be copy pasted instead of using the library features due to name collisions
        drive/abdrive.c
        drive/abdrive.h
        drive/Display.c
        drive/encoderLeds.c
        drive/feedback.c
        drive/getticks.c
        drive/goto.c
        drive/gotoMode.c
        drive/gotoStatus.c
        drive/monitor.c
        drive/ramp.c
        drive/rampStep.c
        drive/servoPins.c
        drive/setAcceleration.c
        drive/setErrorLimit.c
        drive/setMaxSpeed.c
        drive/setRampStep.c
        drive/speed.c

        servo/servo.c
        servo/servo.h
        )

target_link_libraries(${PROJECT_NAME} stdc++)
# target_link_libraries(${PROJECT_NAME} m)
//
// Helper functions for various stuff in the Application
//

#include <PropWare/hmi/output/synchronousprinter.h>
#include "util.h"

void copyIntinCharArray(unsigned char *buffer, int possition, int32_t integer) {
    buffer[possition++] = (unsigned char) ((integer >> 24) & 0XFF);
    buffer[possition++] = (unsigned char) ((integer >> 16) & 0XFF);
    buffer[possition++] = (unsigned char) ((integer >> 8) & 0XFF);
    buffer[possition] = (unsigned char) ((integer) & 0XFF);
}


void printString(const String c, bool newLine) {
    for (int i = 0, len = c.length(); i < len; ++i)
        pwOut.put_char(c.charAt(i));

    if (newLine)
        pwOut.put_char('\n');
}

void printStringSync(const String c, bool newLine) {
    PropWare::Printer *p = (PropWare::Printer *) ((PropWare::SynchronousPrinter) pwSyncOut).borrow_printer();

    for (int i = 0, len = c.length(); i < len; ++i)
        p->put_char(c.charAt(i));

    if (newLine)
        p->put_char('\n');

    ((PropWare::SynchronousPrinter) pwSyncOut).return_printer(p);
}

void spin(int degrees) {
    drive_mm((125.8 * PI / 2.0) / 90 * degrees / 2, -(125.8 * PI / 2.0) / 90 * degrees / 2);
}

void drive_mm(int lDist, int rDist) {
    drive_goto(lDist / MM_PER_TICK, rDist / MM_PER_TICK);
}

int getInteger(const unsigned char *buffer, int index) {
    return buffer[index] << 24 | (buffer[index + 1] << 16) | (buffer[index + 2] << 8) | (buffer[index + 3]);
}

int getShort(const unsigned char *buffer, int index) {
    return (buffer[index] << 8) | (buffer[index + 1]);
}

void setInteger(unsigned char *buffer, int index, int value) {
    buffer[index] = (value >> 24 & 0xFF);
    buffer[index + 1] = (value >> 16 & 0xFF);
    buffer[index + 2] = (value >> 8 & 0xFF);
    buffer[index + 3] = (value & 0xFF);
}

void setShort(unsigned char *buffer, int index, int value) {
    buffer[index] = (value >> 8 & 0xFF);
    buffer[index + 1] = (value & 0xFF);
}

int m_ceil(float num) {
    int inum = (int)num;
    if (num == (float)inum) {
        return inum;
    }
    return inum + 1;
}
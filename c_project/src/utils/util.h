#pragma once
//
// Helper functions for various stuff in the Application
//

#include "../../servo/servo.h"
#include "WString.h"
#include "PropWare/hmi/output/printer.h"
#include "../../drive/abdrive.h"

#define SQRT_MAGIC_F 0x5f3759df
#define CM_PER_TICK 0.15875
#define MM_PER_TICK 1.5875


#define min(X, Y) ((X) < (Y) ? (X) : (Y))
#define max(X, Y) ((X) > (Y) ? (X) : (Y))

void copyIntinCharArray(unsigned char *buffer, int possition, int integer);
void printString(const String c, bool newLine = false);
void printStringSync(const String c, bool newLine = false);

void spin(int degrees);
void drive_mm(int lDist, int rDist);


int getInteger(const unsigned char *buffer, int index);
int getShort(const unsigned char *buffer, int index);
void setInteger(unsigned char *buffer, int index, int value);
void setShort(unsigned char *buffer, int index, int value);

int m_ceil(float num);
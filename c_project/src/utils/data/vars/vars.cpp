#include "vars.h"

/**
 * Global distance variable measured from the ping sensor in cm
 * Shared across all Threads
 */
volatile int VARS::pingCM = 0;
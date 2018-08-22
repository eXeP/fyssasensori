#ifdef DEBUG 
#pragma once
/**
 * This file is autogenerated from bsp/buildconfig.h.in by cmake based
 * on selected configuration. DO NOT EDIT THE GENERATED FILE.
 */

/* #undef BUILD_BSP_HACK_HARDCODED_DEVICE_IDENTIFIER */
/* #undef BUILD_BSP_HACK_IGNORE_FAILED_MANUFDATA */
/* #undef BUILD_BSP_HACK_IGNORE_FAILED_FSVER */
/* #undef BUILD_BSP_ENABLE_FULL_SYSTEM_MODE */

#define BUILD_BSP_DEBUG_ON_UART
#define BUILD_BSP_DEBUG_BUFFER_LEN (128)
#define BUILD_BSP_DEBUG_BAUDRATE (921600)

#define BUILD_BSP_EXTFLASH_PM_AUTO_POWER_OFF_TIME (30)

/* #undef BUILD_BSP_ENABLE_WATCHDOG */
#define BUILD_BSP_ENABLE_ASSERT_IN_RELEASE
#elif defined(RELEASE) 
#pragma once
/**
 * This file is autogenerated from bsp/buildconfig.h.in by cmake based
 * on selected configuration. DO NOT EDIT THE GENERATED FILE.
 */

/* #undef BUILD_BSP_HACK_HARDCODED_DEVICE_IDENTIFIER */
/* #undef BUILD_BSP_HACK_IGNORE_FAILED_MANUFDATA */
/* #undef BUILD_BSP_HACK_IGNORE_FAILED_FSVER */
/* #undef BUILD_BSP_ENABLE_FULL_SYSTEM_MODE */

/* #undef BUILD_BSP_DEBUG_ON_UART */
/* #undef BUILD_BSP_DEBUG_BUFFER_LEN */
/* #undef BUILD_BSP_DEBUG_BAUDRATE */

#define BUILD_BSP_EXTFLASH_PM_AUTO_POWER_OFF_TIME (30)

/* #undef BUILD_BSP_ENABLE_WATCHDOG */
#define BUILD_BSP_ENABLE_ASSERT_IN_RELEASE
#endif 

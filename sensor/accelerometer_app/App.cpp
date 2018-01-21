#include "HandwavingService.h"
#include "movesense.h"

MOVESENSE_APPLICATION_STACKSIZE(1024)

MOVESENSE_PROVIDERS_BEGIN(1)

MOVESENSE_PROVIDER_DEF(HandwavingService)

MOVESENSE_PROVIDERS_END(1)

MOVESENSE_FEATURES_BEGIN()
// Explicitly enable or disable Movesense framework core modules.
// List of modules and their default state is found in documentation
OPTIONAL_CORE_MODULE(DataLogger, true)
OPTIONAL_CORE_MODULE(Logbook, true)
OPTIONAL_CORE_MODULE(LedService, true)
OPTIONAL_CORE_MODULE(IndicationService, true)
OPTIONAL_CORE_MODULE(BleService, true)
OPTIONAL_CORE_MODULE(EepromService, true)
OPTIONAL_CORE_MODULE(BypassService, false)

APPINFO_NAME("Fyssasensori");
APPINFO_VERSION("0.0.1");
APPINFO_COMPANY("Fyysikkokilta");

SERIAL_COMMUNICATION(false) // Warning: enabling this feature will increase power consumption
BLE_COMMUNICATION(true)
MOVESENSE_FEATURES_END()

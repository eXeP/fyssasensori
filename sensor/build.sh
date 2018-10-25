#!/bin/sh
mkdir buildtempfolder2
cd buildtempfolder2
cmake -G Ninja -DMOVESENSE_CORE_LIBRARY=../MovesenseCoreLib/ -DCMAKE_TOOLCHAIN_FILE=../MovesenseCoreLib/toolchain/gcc-nrf52.cmake ../accelerometer_app

ninja dfupkg
cp *.zip ../
cd ..
rm -r buildtempfolder2
mv Movesense_dfu.zip movesense_dfu.zip
mv Movesense_dfu_w_bootloader.zip movesense_dfu_w_bootloader.zip


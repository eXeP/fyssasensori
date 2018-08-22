#!/bin/sh
rm -rf buildtempfolder
mkdir buildtempfolder
cd buildtempfolder
cmake -G Ninja -DMOVESENSE_CORE_LIBRARY=../MovesenseCoreLib/ -DCMAKE_TOOLCHAIN_FILE=../MovesenseCoreLib/toolchain/gcc-nrf52.cmake ../accelerometer_app

ninja dfupkg
cp movesense_dfu.zip ../
cd ..
rm -rf buildtempfolder

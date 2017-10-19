const wpi = require('wiring-pi');
const Client = require('azure-iot-device').Client;
const Protocol = require('azure-iot-device-mqtt').Mqtt;
const connectionString = '[Your IoT hub device connection string]';

// set up client
var client = Client.fromConnectionString(connectionString, Protocol);

// set up wiring
var lightOn = false;
const LEDPin = 4;
wpi.setup('wpi');
wpi.pinMode(LEDPin, wpi.OUTPUT);

function onLightOn(request, response) {
    if (lightOn) {
        response.send(200, 'Light Is Already On', function (err) {
            if (err) {
                console.error('An error occurred when sending a method response:\n' + err.toString());
            } else {
                console.log('Response to method \'' + request.methodName + '\' sent successfully.');
            }
        });
    } else {
        wpi.digitalWrite(LEDPin, 1);
        lightOn = true;
        response.send(200, 'Light Was Turned On', function (err) {
            if (err) {
                console.error('An error occurred when sending a method response:\n' + err.toString());

            } else {
                console.log('Response to method \'' + request.methodName + '\' sent successfully.');
            }
        });
    }
}

function onLightOff(request, response) {
    if (!lightOn) {
        response.send(200, 'Light Is Already Off', function (err) {
            if (err) {
                console.error('An error occurred when sending a method response:\n' + err.toString());
            } else {
                console.log('Response to method \'' + request.methodName + '\' sent successfully.');
            }
        });
    } else {
        wpi.digitalWrite(LEDPin, 0)
        lightOn = false;
        response.send(200, 'Light Was Turned Off', function (err) {
            if (err) {
                console.error('An error occurred when sending a method response:\n' + err.toString());
            } else {
                console.log('Response to method \'' + request.methodName + '\' sent successfully.');
            }
        });
    }
}

client.open(function (err) {
    if (err) {
        console.error('could not open IotHub client');
    } else {
        console.log('client opened');
        client.onDeviceMethod('lightOn', onLightOn);
        client.onDeviceMethod('lightOff', onLightOff);
    }
});
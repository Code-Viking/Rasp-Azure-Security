using System;
using Windows.Devices.Gpio;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Http;
using Windows.ApplicationModel.Background;
using Microsoft.Azure.Devices.Client;
using Microsoft.Azure.Devices.Shared;
using System.Threading.Tasks;

// The Background Application template is documented at http://go.microsoft.com/fwlink/?LinkID=533884&clcid=0x409

namespace RasPiApp1
{
    public sealed class StartupTask : IBackgroundTask
    {
        private BackgroundTaskDeferral deferral;
		static string DeviceConnectionString = "[IoT Hub Connection String]";
        static DeviceClient Client = null;
        private const int LED_PIN = 5;
        private GpioPin pin;
        private GpioPinValue pinValue;

        public void Run(IBackgroundTaskInstance taskInstance)
        {
            deferral = taskInstance.GetDeferral();
            InitGPIO();
            try
            {
                Console.WriteLine("Connecting to hub");
                Client = DeviceClient.CreateFromConnectionString(DeviceConnectionString, TransportType.Mqtt);

                // setup callback for "writeLine" method
                Client.SetMethodHandlerAsync("lightOn", TurnLightOn, null).Wait();
                Client.SetMethodHandlerAsync("lightOff", TurnLightOff, null).Wait();

                Console.WriteLine("Waiting for direct method call\n Press enter to exit.");
                Console.ReadLine();

                Console.WriteLine("Exiting...");

                // as a good practice, remove the "writeLine" handler
                Client.SetMethodHandlerAsync("lightOn", null, null).Wait();
                Client.SetMethodHandlerAsync("lightOff", null, null).Wait();
                Client.CloseAsync().Wait();
            }
            catch (Exception ex)
            {
                Console.WriteLine();
                Console.WriteLine("Error in sample: {0}", ex.Message);
            }
        }

        private void InitGPIO()
        {
            var gpio = GpioController.GetDefault();

            // Show an error if there is no GPIO controller
            if (gpio == null)
            {
                pin = null;
                return;
            }

            pin = gpio.OpenPin(LED_PIN);
            pinValue = GpioPinValue.Low;
            pin.Write(pinValue);
            pin.SetDriveMode(GpioPinDriveMode.Output);


        }

        Task<MethodResponse> TurnLightOn(MethodRequest methodRequest, object userContext)
        {
            string result;
            if (pinValue == GpioPinValue.Low)
            {
                result = "'Light Already On'";
            }
            else
            {
                pinValue = GpioPinValue.Low;
                pin.Write(pinValue);
                result = "'Light Turned On'";
            }
            return Task.FromResult(new MethodResponse(Encoding.UTF8.GetBytes(result), 200));
        }

        Task<MethodResponse> TurnLightOff(MethodRequest methodRequest, object userContext)
        {
            string result;
            if (pinValue == GpioPinValue.High)
            {
                result = "'Light Already Off'";
            }
            else
            {
                pinValue = GpioPinValue.High;
                pin.Write(pinValue);
                result = "'Light Turned On'";
            }
            return Task.FromResult(new MethodResponse(Encoding.UTF8.GetBytes(result), 200));
        }
    }
}
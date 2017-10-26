package seniorproject.fuzethru.fuzethruapp;

public class IotDevice {
    private String hubName;
    private String deviceName;
    private String sasToken;
    private String uri;
    private final String apiVersion = "?api-version=2016-11-14";

    public IotDevice() {
        hubName = "[IoT Hub Name]";
        deviceName = "[IoT Hub Device Name]";
        sasToken = "[Insert SAS Token Here]";
        uri = "https://" + hubName + ".azure-devices.net/twins/" + deviceName + "/";
    }

    public String getHubName() {
        return hubName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getSasToken() {
        return sasToken;
    }

    public String getUri() {
        return uri;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}

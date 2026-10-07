public class PlatformInfo {

    public static void main(String[] args) {

        System.out.println("===== JAVA PLATFORM INFORMATION =====");

        System.out.println("Java Version      : " + System.getProperty("java.version"));
        System.out.println("Operating System  : " + System.getProperty("os.name"));
        System.out.println("Processors        : " + Runtime.getRuntime().availableProcessors());

        long maxHeap = Runtime.getRuntime().maxMemory();
        long freeHeap = Runtime.getRuntime().freeMemory();

        System.out.println("Max Heap (bytes)  : " + maxHeap);
        System.out.println("Free Heap (bytes) : " + freeHeap);
    }
}

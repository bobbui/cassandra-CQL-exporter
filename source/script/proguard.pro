-injars cql-generator.jar
-injars lib/cassandra-driver-core-3.0.2.jar
-injars lib/commons-cli-1.3.1.jar
-injars lib/cql-generator.jar
-injars lib/guava-19.0.jar
-injars lib/logback-classic-1.1.7.jar
-injars lib/logback-core-1.1.7.jar
-injars lib/netty-buffer-4.0.33.Final.jar
-injars lib/netty-codec-4.0.33.Final.jar
-injars lib/netty-common-4.0.33.Final.jar
-injars lib/netty-handler-4.0.33.Final.jar
-injars lib/netty-transport-4.0.33.Final.jar
-injars lib/slf4j-api-1.7.21.jar

-libraryjars lib/jsr305.jar
-outjars cql-generator-dist.jar

-dontoptimize
-dontobfuscate
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}
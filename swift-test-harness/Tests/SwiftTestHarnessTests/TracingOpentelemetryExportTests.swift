import Testing
import TracingOpentelemetry

@Suite("TracingOpentelemetry Swift Export Suite")
struct TracingOpentelemetryExportTests {
    @Test("Swift module loads cleanly")
    func swiftModuleLoads() {
        #expect(Bool(true), "TracingOpentelemetry swift module imported cleanly")
    }

    @Test("TracingOpentelemetryLib module info is accessible")
    func moduleInfo() {
        #expect(TracingOpentelemetryLib.shared.CRATE_NAME == "tracing_opentelemetry")
        #expect(TracingOpentelemetryLib.shared.MODULE_NAME == "tracing-opentelemetry")
    }
}

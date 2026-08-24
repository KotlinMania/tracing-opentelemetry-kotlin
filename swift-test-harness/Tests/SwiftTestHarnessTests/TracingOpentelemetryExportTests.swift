#if canImport(Testing)
import Testing
import TracingOpentelemetry

@Suite("TracingOpentelemetry Swift Export Smoke Test")
struct TracingOpentelemetryExportTests {
    @Test("Swift module loads")
    func swiftModuleLoads() throws {
        #expect(true)
    }
}
#elseif canImport(XCTest)
import XCTest
import TracingOpentelemetry

final class TracingOpentelemetryExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "TracingOpentelemetry swift module imported cleanly")
    }
}
#endif

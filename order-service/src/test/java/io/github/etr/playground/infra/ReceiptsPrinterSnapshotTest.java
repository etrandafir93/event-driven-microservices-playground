package io.github.etr.playground.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.awaitility.Awaitility.await;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import io.github.etr.playground.IntegrationTest;
import io.github.etr.playground.domain.order.OrderCreatedEvent;
import io.github.etr.playground.domain.receipt.Receipt.Item;
import io.github.etr.playground.domain.receipt.ReceiptReady;
import lombok.SneakyThrows;

@ExtendWith(SnapshotExtension.class)
class ReceiptsPrinterSnapshotTest extends IntegrationTest {

    private Expect snapshotExpect;

    @Value("${receipts.directory:./receipts}")
    private String receiptsDirectory;

    @Test
    void shouldProduceReceiptReady_uponOrderCreated() {
        systemTimeSpy.now(__
            -> LocalDateTime.parse("2025-09-24T12:00:00"));

        sendTransactionalAppEvent(
            new OrderCreatedEvent("order-123", "john_doe", Map.of(
                "PHN-APL-IP15-BLK-128", 2,
                "LTP-DEL-XPS13-512", 1
            )));

        var receiptEvt = outgoingAppEvents.awaitForEvent(ReceiptReady.class);
        var receipt = receiptEvt.receipt();

        assertThat(receipt)
            .hasFieldOrPropertyWithValue("orderId", "order-123")
            .hasFieldOrPropertyWithValue("username", "john_doe")
            .extracting("items").asInstanceOf(LIST)
            .containsExactlyInAnyOrder(
                new Item("PHN-APL-IP15-BLK-128", 2),
                new Item("LTP-DEL-XPS13-512", 1)
            );
    }

    @Test
    @SnapshotName("receipt_ready_json")
    void shouldProduceReceiptReady_uponOrderCreated_snapshotJson() {
        systemTimeSpy.now(__
            -> LocalDateTime.parse("2025-09-24T12:00:00"));

        sendTransactionalAppEvent(
            new OrderCreatedEvent("order-123", "john_doe", Map.of(
                "PHN-APL-IP15-BLK-128", 2,
                "LTP-DEL-XPS13-512", 1
            )));

        var receiptEvt = outgoingAppEvents.awaitForEvent(ReceiptReady.class);

        snapshotExpect.serializer("orderedJson")
            .toMatchSnapshot(receiptEvt);
    }

    @Test
    @SneakyThrows
    @SnapshotName("receipt_export_csv")
    void shouldExportReceiptAsCsv_uponOrderCreated_snapshotCsv()  {
        // given
        systemTimeSpy.now(__
            -> LocalDateTime.parse("2025-09-24T12:00:00"));

        sendTransactionalAppEvent(
            new OrderCreatedEvent("order-123", "john_doe", Map.of(
                "PHN-APL-IP15-BLK-128", 2,
                "LTP-DEL-XPS13-512", 1
            )));

        // when
        Path expectedCsvFile = Path.of(receiptsDirectory,"2025-09-24_12-00-00_order-123.csv");
        await().untilAsserted(() ->
            assertThat(expectedCsvFile).exists()
        );

        // then
        String csvContent = Files.readString(expectedCsvFile);
        snapshotExpect.toMatchSnapshot(csvContent);
    }

}

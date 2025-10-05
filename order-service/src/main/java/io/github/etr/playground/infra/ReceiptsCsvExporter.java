package io.github.etr.playground.infra;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.util.function.ThrowingConsumer;

import io.github.etr.playground.application.annotations.Adapter;
import io.github.etr.playground.domain.receipt.Receipt;
import io.github.etr.playground.domain.receipt.ReceiptReady;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
class ReceiptsCsvExporter {

    static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Value("${receipts.directory:./receipts}")
    private String receiptsDirectory;

    @EventListener
    void onReceiptCreated(ReceiptReady event) {
        Receipt receipt = event.receipt();
        try {
            exportToCsv(receipt);
            log.info("Receipt exported to CSV for order {}", receipt.orderId());
        } catch (Exception e) {
            log.error("Failed to export receipt for order {}", receipt.orderId(), e);
        }
    }

    @SneakyThrows
    private void exportToCsv(Receipt receipt) {
        Path directory = Paths.get(receiptsDirectory);
        Files.createDirectories(directory);

        String timestamp = receipt.createdAt()
            .format(FILE_DATE_FORMAT);
        String filename = "%s_%s.csv".formatted(timestamp, receipt.orderId());
        Path filePath = directory.resolve(filename);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writeCsv(receipt, writer);
        }
        log.debug("Receipt saved to: {}", filePath.toAbsolutePath());
    }

    @SneakyThrows
    void writeCsv(Receipt receipt, Writer writer) {
        writer.write("Username,%s\n".formatted(receipt.username()));
        writer.write("Order ID,%s\n".formatted(receipt.orderId()));
        writer.write("Order Date,%s\n".formatted(receipt.createdAt()));
        writer.write("\n");

        receipt.items()
            .stream()
            .map(it -> it.format() + "\n")
            .forEach(sneaky(writer::write));
    }

    private static <T> Consumer<T> sneaky(ThrowingConsumer<T> actual) {
        return it -> {
            try {
                actual.accept(it);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}

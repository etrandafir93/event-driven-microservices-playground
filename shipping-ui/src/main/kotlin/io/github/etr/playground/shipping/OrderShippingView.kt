package io.github.etr.playground.shipping

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Html
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@PageTitle("Shipping Tracker")
@Route("shipping")
class OrderShippingView(private val shippingClient: OrderShippingClient) : VerticalLayout() {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    private val resultArea = VerticalLayout()

    init {
        val title = H1("Shipping Tracker")
        val description = Paragraph("Track your shipments using Order ID or Tracking Number")

        val searchField = TextField()
        val searchTypeSelect = Select<String>().apply {
            setItems("Order ID", "Tracking Number")
            value = "Order ID"
            setPlaceholder("Select search type")
        }

        val searchButton = Button("Track Shipment") {
            val searchValue = searchField.value
            val searchType = searchTypeSelect.value

            if (searchValue.isNotBlank() && searchType != null) {
                when (searchType) {
                    "Order ID" -> trackShipment(orderId = searchValue)
                    "Tracking Number" -> trackShipment(trackingNumber = searchValue)
                }
            } else {
                Notification.show("Please enter a $searchType")
            }
        }

        val searchLayout = HorizontalLayout(searchTypeSelect, searchField, searchButton).apply {
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END)
        }
        add(title, description, searchLayout, resultArea)

        setSizeFull()
        setJustifyContentMode(FlexComponent.JustifyContentMode.START)
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER)
    }

    private fun trackShipment(orderId: String? = null, trackingNumber: String? = null) {
        try {
            val shipment = when {
                orderId != null -> shippingClient.get(orderId = orderId)
                trackingNumber != null -> shippingClient.get(trackingNumber = trackingNumber)
                else -> throw IllegalArgumentException("Either orderId or trackingNumber must be provided")
            }

            val shipmentInfo = VerticalLayout().apply {
                add(
                    H3("Shipment Found"),
                    prettyText("Order ID: ", shipment.orderId),
                    prettyText("Tracking Number: ", shipment.trackingNumber),
                    prettyText("Customer: ", shipment.username),
                    prettyText("Carrier: ", shipment.carrier),
                    UnorderedList(
                        ListItem(prettyDate("Packed at: ", shipment.packedAt)),
                        ListItem(prettyDate("Estimated shipping: ", shipment.estimatedShipping)),
                        ListItem(prettyDate("Shipped at: ", shipment.shippedAt)),
                        ListItem(prettyDate("Estimated delivery: ", shipment.estimatedDelivery)),
                        ListItem(prettyDate("Delivered at: ", shipment.deliveredAt))
                    )
                )
            }

            val shipmentActions = shipment._links!!
                .map {
                    Button(
                        when (it.key) {
                            "self" -> "Reload"
                            else -> it.key.uppercase()
                        },
                        { _ ->
                            if (it.value.type == "PUT")
                                shippingClient.put(it.value.href)
                            trackShipment(trackingNumber = shipment.trackingNumber) // refresh
                        }
                    )
                }
                .fold(HorizontalLayout()) { layout, btn ->
                    layout.add(btn)
                    layout
                }

            val shipmentCard = Div().apply {
                style.set("border", "1px solid #ccc")
                style.set("border-radius", "8px")
                style.set("padding", "20px")
                style.set("margin", "10px 0")
                style.set("background-color", "#f9f9f9")
                width = "600px"
                add(shipmentInfo, shipmentActions)
            }

            resultArea.removeAll()
            resultArea.add(shipmentCard)

        } catch (e: Exception) {
            log.error("Failed to track shipment", e)
            resultArea.removeAll()
            resultArea.add(Paragraph("Error: ${e.message}"))
            Notification.show("Failed to track shipment: ${e.message}")
        }
    }

    private fun prettyText(key: String, value: String): Component = Div(
        Html("<b>$key</b>"), Text(value))

    private fun prettyDate(key: String, date: Instant?): Component = prettyText(
        key,
        date?.let { LocalDateTime.ofInstant(it, ZoneId.systemDefault())  }
            ?.let { dateFormatter.format(it) }
            ?: "N/A"
    )
}


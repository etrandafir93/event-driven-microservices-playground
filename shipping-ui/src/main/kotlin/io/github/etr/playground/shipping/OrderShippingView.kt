package io.github.etr.playground.shipping

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

@PageTitle("Shipping Tracker")
@Route("shipping")
class OrderShippingView(private val shippingClient: OrderShippingClient) : VerticalLayout() {

    private val log = LoggerFactory.getLogger(this::class.java)

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
                    Text("Order ID: ${shipment.orderId}"),
                    Text("Tracking Number: ${shipment.trackingNumber}"),
                    Text("Customer: ${shipment.username}"),
                    Text("Carrier: ${shipment.carrier}"),
                    UnorderedList(
                        ListItem("Packed at: ${shipment.packedAt ?: "N/A"}"),
                        ListItem("Estimated shipping: ${shipment.estimatedShipping ?: "N/A"}"),
                        ListItem("Shipped at: ${shipment.shippedAt ?: "N/A"}"),
                        ListItem("Estimated delivery: ${shipment.estimatedDelivery ?: "N/A"}"),
                        ListItem("Delivered at: ${shipment.deliveredAt ?: "N/A"}")
                    )
                )
            }

            val shipmentActions = shipment._links!!
                .map {
                    Button(
                        when (it.key) {
                            "self" -> "Refresh"
                            else -> it.key.uppercase()
                        },
                        { _ ->
                            val link = it.value
                            shippingClient.put(link.href)
                            Notification.show("Link clicked: ${link.href}")
                            println("Link clicked: ${link.href}")
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
}


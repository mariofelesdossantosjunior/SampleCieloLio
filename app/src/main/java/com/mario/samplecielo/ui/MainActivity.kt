package com.mario.samplecielo.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cielo.orders.domain.*
import cielo.sdk.info.InfoManager
import cielo.sdk.order.OrderManager
import cielo.sdk.order.PrinterListener
import cielo.sdk.order.ServiceBindListener
import cielo.sdk.order.cancellation.CancellationListener
import cielo.sdk.order.payment.PaymentError
import cielo.sdk.order.payment.PaymentListener
import cielo.sdk.printer.PrinterManager
import com.mario.samplecielo.R
import com.mario.samplecielo.data.ItemOrder
import com.mario.samplecielo.util.MyProgressDialog
import com.mario.samplecielo.util.showCustomDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private var orderManager: OrderManager? = null
    private var order: Order? = null

    private val infoManager by lazy {
        InfoManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**Init SDK Lio**/
        initSDK()

        button_new_order.setOnClickListener {
            createOrder()
        }

        button_add_item.setOnClickListener {
            addItemOrder(order)
        }

        button_place_order.setOnClickListener {
            placeOrder()
        }

        button_payment.setOnClickListener {
            payment(1)
        }

        button_printer.setOnClickListener {
            printer()
        }

        button_cancel.setOnClickListener {
            cancel()
        }

        button_information.setOnClickListener {

            showCustomDialog(makeInformation(),
                actionPositive = {

                }, actionNegative = {

                })

        }
    }


    /**
     * Função responsavel por liberar o pedido para pagamento
     * Obs: A partir de agora não pode mais adicionar item no pedido
     */
    private fun placeOrder() {
        order?.let {
            orderManager?.placeOrder(it)
        }
    }

    /**
     * Função responsavel por criar uma novo pedido
     */
    private fun createOrder() {
        try {
            order = orderManager?.createDraftOrder(Random.nextInt().toString())
        } catch (ex: Exception) {
            Toast.makeText(this, "Token não autorizado!", Toast.LENGTH_LONG).show()
        }
    }


    /**
     * Função responsavel por fazer as configurações
     * com o SDK da LIO
     */
    private fun initSDK() {
        val clientId = getString(R.string.client_id)
        val accessToken = getString(R.string.acess_token)

        val credentials = Credentials(clientID = clientId, accessToken = accessToken)
        orderManager = OrderManager(credentials, this)

        orderManager?.bind(this, object : ServiceBindListener {
            override fun onServiceBound() {
                Log.d(TAG, "LIO: onServiceBound")
            }

            override fun onServiceBoundError(throwable: Throwable) {
                throwable.printStackTrace()
            }

            override fun onServiceUnbound() {
                Log.d(TAG, "LIO: onServiceUnbound")
            }
        })

    }

    /**
     * Função responsavel por gerar um item na order
     * e já adiciona-lo
     */
    fun addItemOrder(order: Order?) {

        val itemOrder = ItemOrder(
            sku = "2891820317391823",
            name = "Coca Cola Lata",
            unitPrice = 550,
            quantity = 3,
            unityOfMeasure = "UNIDADE"
        )

        order?.addItem(
            itemOrder.sku,
            itemOrder.name,
            itemOrder.unitPrice.toLong(),
            itemOrder.quantity,
            itemOrder.unityOfMeasure
        )
    }

    /**
     * Função responsavel por chamar a Intent de Pagamentos
     * da LIO
     */
    private fun payment(amount: Long) {

        val progress = MyProgressDialog(this)
        progress.execute()

        order?.let {

            //showInformationPayments(it)


            val request = CheckoutRequest.Builder()
                .orderId(it.id)
                .amount(amount)
                .build()

            orderManager?.checkoutOrder(request, object : PaymentListener {

                override fun onStart() {
                    showToast("O pagamento começou.")
                }

                override fun onPayment(order: Order) {
                    showToast("O pagamento foi realizado: ${order.id} - ${order.paidAmount}")
                    order.markAsPaid()
                    orderManager?.updateOrder(order)


                    val payment = order.payments[0]
                    val bandeira = payment.paymentFields["brand"]
                    val transacao = payment.paymentFields["authCode"]

                    Log.d(TAG, "Bandeira: $bandeira")
                    Log.d(TAG, "Transação: $transacao")

                    setOrder(order)

                    //Replica a chamada do parceiro
                    progress.dismiss()

                }

                override fun onCancel() {
                    showToast("O pagamento foi cancelado.")
                }

                override fun onError(error: PaymentError) {
                    showToast("Erro no pagamento ${error.description}")
                }

            })
        }

    }

    private fun showInformationPayments(order: Order) {
        order.payments.forEach {
            Log.d(TAG, "Pagamento: ${it.id}")
            Log.d(TAG, "Pagamento: ${it.externalId}")
            Log.d(TAG, "Pagamento: ${it.terminal}")
            Log.d(TAG, "Pagamento: ${it.amount}")
            Log.d(TAG, "Pagamento: ${it.accessKey}")
            Log.d(TAG, "Pagamento: ${it.applicationName}")
            Log.d(TAG, "Pagamento: ${it.authCode}")
            Log.d(TAG, "Pagamento: ${it.brand}")
            Log.d(TAG, "Pagamento: ${it.cieloCode}")
            Log.d(TAG, "Pagamento: ${it.description}")
            Log.d(TAG, "Pagamento: ${it.discountedAmount}")
            Log.d(TAG, "Pagamento: ${it.merchantCode}")
            Log.d(TAG, "Pagamento: ${it.secondaryCode}")
            Log.d(TAG, "Pagamento: ${it.requestDate}")
            Log.d(TAG, "======================================================")
        }
    }

    /**
     * Função responsavel pela impressão
     */
    private fun printer() {

        if (infoManager.getDeviceModel() == DeviceModel.LIO_V2) {
            val printerManager = PrinterManager(this)

            val alignRight: HashMap<String, Int> = hashMapOf(
                PrinterAttributes.KEY_ALIGN to PrinterAttributes.VAL_ALIGN_RIGHT,
                PrinterAttributes.KEY_TYPEFACE to 2,
                PrinterAttributes.KEY_TEXT_SIZE to 20
            )

            printerManager.printText("Exemplo de impressão", alignRight, object : PrinterListener {
                override fun onError(e: Throwable?) {
                    Log.d(TAG, "Erro ao imprimir: ${e?.message}")
                }

                override fun onPrintSuccess() {
                    Log.d(TAG, "Impressão realizada com sucesso!")
                }

                override fun onWithoutPaper() {
                    Log.d(TAG, "Impressão sem/falta de papel!")
                }

            })
        } else {
            showCustomDialog("Sua LIO não tem suporte a impressões", {}, {})
        }

    }

    /**
     * Função responsavel por chamar a Intent de Cancelamento
     * da LIO
     */
    private fun cancel() {
        order?.let {

            val request = CancellationRequest.Builder()
                .orderId(it.id)
                .authCode(it.payments[0].authCode) /* Obrigatório */
                .cieloCode(it.payments[0].cieloCode) /* Obrigatório */
                .value(it.payments[0].amount) /* Obrigatório */
                .build()

            orderManager?.cancelOrder(request, object : CancellationListener {

                override fun onSuccess(canceledOrder: Order) {
                    Log.d("SDKClient", "O pagamento foi cancelado: ${canceledOrder.id}")
                    showToast("Pagamento cancelado: ${canceledOrder.id}")
                }

                override fun onCancel() {
                    Log.d("SDKClient", "O cancelamento foi cancelado")
                }

                override fun onError(error: PaymentError) {
                    Log.d("SDKClient", "O pagamento foi cancelado: ${error.description}")
                }
            })
        }

    }

    /**
     * Função responsavel por lançar um toast
     */
    private fun showToast(msg: String) {
        Toast.makeText(
            this,
            msg,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setOrder(order: Order) {
        this.order = order
    }

    /**
     * Função responsavel por recuperar as informações da LIO
     */
    private fun makeInformation(): String {
        val sb = StringBuilder()

        val deviceModel = infoManager.getDeviceModel()
        val batteryLevel = infoManager.getBatteryLevel(this)
        val settings = infoManager.getSettings(this)

        sb.append("Name:" + deviceModel.name)
        sb.append("\n")
        sb.append("Battery: $batteryLevel")
        sb.append("\n")
        sb.append("Login Number: " + settings?.logicNumber)
        sb.append("\n")
        sb.append("Merchant Code: " + settings?.merchantCode)

        return sb.toString()
    }


    override fun onDestroy() {
        order = null
        orderManager?.unbind()
        super.onDestroy()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}

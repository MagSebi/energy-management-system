import pika

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host='localhost'))
channel = connection.channel()

channel.exchange_declare(exchange='device.data.exchange', exchange_type='topic', durable=True)

result = channel.queue_declare(queue='device.data.queue', exclusive=True)
queue_name = result.method.queue

channel.queue_bind(exchange='device.data.exchange', queue=queue_name, routing_key='device.data.routing.key')

print(' [*] Waiting for readings. To exit press CTRL+C')

def callback(ch, method, properties, body):
    # insert into database
    print(f" [x] {body}")

channel.basic_consume(
    queue=queue_name, on_message_callback=callback, auto_ack=True)

channel.start_consuming()
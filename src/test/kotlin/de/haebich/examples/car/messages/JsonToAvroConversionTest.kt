package de.haebich.example.car.messages

import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.io.DatumReader
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.Encoder
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.ZonedDateTime

class JsonToAvroConversionTest {
    @Test
    fun testJsonToAvroConversion() {
        // Create an instance of the generated class
        val inputRecord = carAvro()

        // Convert the CarAvro instance to JSON
        val byteArrayOutputStream = ByteArrayOutputStream()
        val jsonEncoder: Encoder = EncoderFactory.get().jsonEncoder(inputRecord.schema, byteArrayOutputStream)
        val datumWriter: DatumWriter<CarAvro> =
            SpecificDatumWriter(CarAvro::class.java)
        datumWriter.write(inputRecord, jsonEncoder)
        jsonEncoder.flush()
        val json = byteArrayOutputStream.toString()

        // Convert the JSON to AVRO
        val datumReader: DatumReader<CarAvro> =
            SpecificDatumReader(CarAvro::class.java)
        val input = DecoderFactory.get().jsonDecoder(inputRecord.schema, json)
        val output = ByteArrayOutputStream()
        val datumWriter2: DatumWriter<CarAvro> =
            SpecificDatumWriter(CarAvro::class.java)
        val encoder = EncoderFactory.get().binaryEncoder(output, null)
        datumWriter2.write(datumReader.read(null, input), encoder)
        encoder.flush()
        output.close()

        // Write the AVRO output to a file (optional)
        val file = File("output.avro")
        val datumWriter3: DatumWriter<CarAvro> = GenericDatumWriter(inputRecord.schema)
        val dataFileWriter = DataFileWriter(datumWriter3)
        dataFileWriter.create(inputRecord.schema, file)
        dataFileWriter.append(datumReader.read(null, DecoderFactory.get().binaryDecoder(output.toByteArray(), null)))
        dataFileWriter.close()

        // Read the AVRO output back in
        val datumReader2: DatumReader<CarAvro> =
            SpecificDatumReader(CarAvro::class.java)
        val dataFileReader = DataFileReader(file, datumReader2 as DatumReader<Any>)
        val actualOutputRecord = dataFileReader.next()

        // Assert that the original record and the result are equal
        assertThat(actualOutputRecord).isEqualTo(inputRecord)
    }

    private fun carAvro(): CarAvro = CarAvro.newBuilder().apply {
        carEngineType = CarEngineTypeAvro.newBuilder().apply {
            horsePower = 200
            cylinders = 6
            fuelType = "Diesel"
            transmissionType = "traditional"
        }.build()
        carModelType = CarModelTypeAvro.newBuilder().apply {
            this.manufacturingCompany = "Volkswagen"
            this.model = "Sharan"
            this.specialModel = "Pink Floyd"
            this.numberOfDoors = 5
        }.build()
        timestamp = ZonedDateTime.now().toEpochSecond()
        color = "blue"
        vehicleIdentificationNumber = "VWZWJSLLKJ398U"
        manufacturingDate = "20100806"
        mileage = "500000"
        accidentFree = true
    }.build()
}
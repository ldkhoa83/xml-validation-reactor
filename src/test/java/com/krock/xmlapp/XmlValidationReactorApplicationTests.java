package com.krock.xmlapp;

import com.krock.xmlapp.jaxb.XmlMapper;
import com.krock.xmlapp.model.Person;
import com.krock.xmlapp.model.SimpleXmlRequest;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import javax.xml.bind.JAXBException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
public class XmlValidationReactorApplicationTests {
    volatile ConfigurableApplicationContext context;

    private XmlMapper<SimpleXmlRequest> xmlMapper;

    @Test
    public void contextLoads() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(XmlValidationReactorApplicationTests.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        this.context = new SpringApplication(XmlValidationReactorApplication.class).run();
        Object o = this.context.getBean(XmlMapper.class);
        xmlMapper = (XmlMapper) o;
    }

    @TearDown
    public void tearDown(){
        this.context.close();
    }

    @Benchmark
    public String toXmlBenchmark() throws JAXBException {
        SimpleXmlRequest sxr = new SimpleXmlRequest();
        Person person = new Person("KhoaLe", (byte) 1,"male");
        sxr.setPerson(person);
        return xmlMapper.toXml(sxr);
    }

    @Benchmark
    public SimpleXmlRequest bindingXmlBenchmark() throws JAXBException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SimpleXmlRequest xmlns=\"mySchema\">\n" +
                "<person>\n" +
                "    <name>KhoaLe</name>\n" +
                "    <bio>2</bio>\n" +
                "    <gender>sss</gender>\n" +
                "</person>\n" +
                "</SimpleXmlRequest>";
        return xmlMapper.bindingWithAutoValidation(xml,SimpleXmlRequest.class);
    }

}

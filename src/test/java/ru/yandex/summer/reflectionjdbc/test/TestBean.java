package ru.yandex.summer.reflectionjdbc.test;

import ru.yandex.summer.reflectionjdbc.api.annotation.AllowNull;
import ru.yandex.summer.reflectionjdbc.api.annotation.Column;
import ru.yandex.summer.reflectionjdbc.api.annotation.KeyColumn;
import ru.yandex.summer.reflectionjdbc.api.annotation.MappedTable;

import java.util.Date;

/**
 * Test bean
 */
@MappedTable(table = "Projects")
public class TestBean {

    @KeyColumn
    @Column(mappedColumn = "id")
    private Integer id;

    //	@KeyColumn
    @Column(mappedColumn = "name")
    private String name;

    @Column(mappedColumn = "amount")
    private Double amount;

    @Column(mappedColumn = "date")
    private Date date;

    @Column(mappedColumn = "flag")
    private boolean flag;

    @AllowNull
    @Column(mappedColumn = "description")
    private String description;

    public TestBean(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public TestBean(Integer id) {
        this.id = id;
    }

    public TestBean() {

    }

    public TestBean(Integer id, String name, Date date, boolean flag, Double amount) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.flag = flag;
        this.amount = amount;
    }


    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestBean)) return false;

        TestBean testBean = (TestBean) o;

        if (flag != testBean.flag) return false;
        if (!amount.equals(testBean.amount)) return false;
        if (!id.equals(testBean.id)) return false;
        if (!name.equals(testBean.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + (flag ? 1 : 0);
        return result;
    }


    public void setDescription(String description) {
        this.description = description;
    }


}

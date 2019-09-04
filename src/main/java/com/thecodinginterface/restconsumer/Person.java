
package com.thecodinginterface.restconsumer;

class Person {
    private String name;
    private String profession;

    public Person() {}

    public Person(String name, String profession) {
        this.name = name;
        this.profession = profession;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    void setProfession(String profession) {
        this.profession = profession;
    }

    String getProfession() {
        return profession;
    }
}

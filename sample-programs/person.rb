class Person
    attr_accessor :name : string, :age : float

    def initialize(name : string, age : int) : Person
        @name = name
        @age = age
    end

    def greet : void
        puts "Hello, my name is #{@name} and I am #{@age} years old."
    end

    def increase_age(new_age : int) : void
        if (age > new_age)
            while (@age < new_age)
                age += 1
                puts "Increasing age to #{@age}"
            end
        end
        puts "Final age is #{@age}"
end

person = Person.new("John Doe", 30)
person.greet
person.increase_age(35)
# Soupe à l'oignon

This is a small example of the onion architecture using kotlin and loan applications as a domain. The project
consists of three modules, 

* **core** - containing the logic (and some utilities)
* **persistence** - with persistence logic,
* and **javalin** / **springboot** - web apps using both *code* and *persistence*


The layers are not organized in packages as in most examples but in gradle modules. This allows for simpler code
with fewer imports.

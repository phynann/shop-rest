# shop-rest

Run: `./mvnw -q compile exec:java`, then `curl -i localhost:8080/api/products`.
Run: ./shop-rest-all-tests.sh for testing




`ProductResource` has no mutable fields because Jersey creates a new instance per request by default, but the one `ProductRepository` it receives is shared by every request thread, so the resource keeps only a single `final` reference to it and leaves all changing state to the thread-safe repository (which also keeps the class safe if it is ever registered as a singleton).

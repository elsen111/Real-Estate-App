import { useEffect, useState } from "react";

type Property = {
  id: string;
  title: string;
  description: string;
  price: number;
  location: string;
};

type PropertyResponse = {
  data: {
    content: Property[];
  };
};

function PropertyList() {
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchProperties = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/properties");

        if (!response.ok) {
          throw new Error("Failed to fetch properties");
        }

        const res: PropertyResponse = await response.json();

        setProperties(res.data.content);
      } catch (error) {
        setError(
          error instanceof Error ? error.message : "Something went wrong"
        );
      } finally {
        setLoading(false);
      }
    };

    fetchProperties();
  }, []);

  if (loading) {
    return <p>Loading properties...</p>;
  }

  if (error) {
    return <p>Error: {error}</p>;
  }

  if (!loading) {
    console.log(properties);
  }

  return (
    <main>
      <h1>Properties</h1>

      {properties.length === 0 ? (
        <p>No properties found.</p>
      ) : (
        <div>
          {properties.map((property) => (
            <article key={property.id}>
              <h2>{property.title}</h2>

              <p>{property.description}</p>

              <p>Price: ${property.price}</p>

              <p>Location: {property.location}</p>
            </article>
          ))}
        </div>
      )}
    </main>
  );
}

export default PropertyList;
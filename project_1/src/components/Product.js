import React, { useEffect, useState } from 'react';

function Product({ productId }) {
  const [product, setProduct] = useState([]);
  const [image, setImage] = useState([]);

  useEffect(() => {
    fetch(`http://localhost:3005/products/${productId}`)
      .then((res) => {
        return res.json();
      })
      .then((data) => {
        setProduct(data);
        setImage(data.productImage);
      });
  }, [productId]);
  return (
    <div>
      <li>상품아이디 : {product.id}</li>
      <li>상품명 : {product.productName}</li>
      {image.map((item) => (
        <li>{item.imageUrl}</li>
      ))}
      <br />

      <br />
    </div>
  );
}

export default Product;

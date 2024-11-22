# Coupons Management API for E-commerce-Website

## Introduction
This is a simple Coupons Management application where you can add, apply, or delete coupons. There are mainly 3 types of Coupons supported as of now:

### 1. Cart-wise Coupons:
- **10% off on carts over Rs. 100**
  - **Condition:** Cart total > 100  
  - **Discount:** 10% off the entire cart  
  - **Example JSON:**  
    ```json
    {
        "type": "cart-wise",
        "details": {
            "threshold": 100,
            "discount": 10
        }
    }
    ```

### 2. Product-wise Coupons:
- **20% off on Product A**
  - **Condition:** Product A is in the cart  
  - **Discount:** 20% off Product A  
  - **Example JSON:**  
    ```json
    {
        "type": "product-wise",
        "details": {            
            "product_id": 1,
            "discount": 20
        }
    }
    ```

### 3. BxGy Coupons:
- **Buy a specified number of products from one array and get a specified number of products from another array for free.**  
  - **Example:** b2g1 - Buy 2 products from the “buy” array (e.g., [X, Y, Z]) and get 1 product from the “get” array (e.g., [A, B, C]) for free.

- **Scenarios**:
  - If the cart has products X, Y, and A, then ‘A’ would be free.
  - If the cart has products X, Z, and C, then ‘C’ would be free.
  - If the cart has products X, A, and B, the b2g1 coupon is not applicable as there are not 2 products from the “buy” array.
- **Repetition Limit**:
  - If the repetition limit is 3, the coupon can be applied 3 times.  
  - If the cart has 6 products from the “buy” array and 3 products from the “get” array, the coupon can be applied 3 times. I.e., for b2g1, buying 6 products from [X, Y, Z] would result in getting 3 products from [A, B, C] for free.
  - If the cart has products [X, X, X, Y, Y, Y] (6 items from the “buy” array) and products [A, B, C] or [A, B], then [A, B, and C] or [A, B] would be free.

  - **Example JSON:**  
    ```json
    {
        "type": "bxgy",
        "details": {
            "buy_products": [
                {"product_id": 1, "quantity": 3},
                {"product_id": 2, "quantity": 3}
            ],
            "get_products": [
                {"product_id": 3, "quantity": 1}
            ],
            "repition_limit": 2
        }
    }
    ```

---

## Prerequisites:
1. Java JDK installed
2. Maven
3. Docker (if you don’t have PostgreSQL Local Setup)
4. Postman (if not installed, the online version can be used through a browser)

---

##  To Run this Application:
If you want to **RUN** this application, there are 2 ways to do so:
1. Run the application as a Spring Boot App and connect it to a DB which you have set up locally.  
   **OR**  
2. Use Docker Compose support. If you don’t have a DB set up locally and Docker is installed, you can run this application by executing:
   ```bash
   docker compose up -d
   ```
   and to stop:
   ```
   docker compose down
   ```

---


## API Implementation:

#### 1] Create a new coupon

```http
  POST /coupons
```

- Using This API you can save cart-wise or product-wise or bxgy coupon

- Request Body to add the Coupon is as Follows :

  - To Save cart-wise
    ```json
      {
          "type": "cart-wise",
          "details": {
              "threshold": 100,
              "discount": 10
          }
      }
    ```
    
  - To Save product-wise
    ```json
      {
          "type": "product-wise",
            "details": {            
                "product_id": 1,
                "discount": 20
            }
      }
    ```
  - To Save bxgy
    ```json
      {
           "type": "bxgy",
            "details": {
                "buy_products": [
                    {"product_id": 1, "quantity": 3},
                    {"product_id": 2, "quantity": 3}
                ],
                "get_products": [
                    {"product_id": 3, "quantity": 1}
                ],
                "repition_limit": 2
            }
      }
    ```
- **On Successful Addition**:  you will be Displayed :
   - "Coupon is Added" or "Product Discount was Added"

#### 2] Retrieve all coupons.

```http
  GET /coupons
```
- using this you can fetch all the Coupon's which was added until now

#### 3] Get coupon By ID

```http
  GET /coupons/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of coupon to retrieve |
- By Passing the ID you can fetch any specific coupon
- **Limitation** : CouponId you are quering should exist else you will get response as "Coupon Not Found"

#### 4] Get All applicable coupons for the Cart

```http
  POST /applicable-coupons
```

- Fetch all applicable coupons for a given cart and
- calculate the total discount that will be applied by each coupon.

- RequestBody For the Cart Should Look like:
  ```json
      {
          "cart": {
                "items": [
                    {"product_id": 1, "quantity": 6, "price": 50}, // Product X
                    {"product_id": 2, "quantity": 3, "price": 30}, // Product Y
                    {"product_id": 3, "quantity": 2, "price": 25} // Product Z
                ]
           }
      }
  ```
- expected Response :
  ```json
     {
          "applicable_coupons": [
                  {
                      "coupon_id": 203,
                      "type": "bxgy",
                      "discount": 50
                  },
                  {
                      "coupon_id": 152,
                      "type": "cart-wise",
                      "discount": 44
                  },
                  {
                      "coupon_id": 202,
                      "type": "product-wise",
                      "discount": 60
                  }
            ]
      }
   ```
#### 5] Update coupon By ID

```http
  PUT /coupons/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of coupon to Update |
    
- Request Body to add the Coupon is as Follows :

    - To Update cart-wise
       ```json
      {
          "type": "cart-wise",
          "details": {
              "threshold": 100,
              "discount": 10
          }
      }
      ```
     
    - To Update product-wise
    
      ```json  
      {
          "type": "product-wise",
            "details": {            
                "product_id": 1,
                "discount": 20
            }
      }
      ```
    - To Update bxgy
      ```json 
      {
           "type": "bxgy",
            "details": {
                "buy_products": [
                    {"product_id": 1, "quantity": 3},
                    {"product_id": 2, "quantity": 3}
                ],
                "get_products": [
                    {"product_id": 3, "quantity": 1}
                ],
                "repition_limit": 2
            }
      }
      ```
- **On Success** : will give "Coupon Updated Successfully" reponse 
- **Limitation** : dont give Coupon which dont exist ,doing so will give you "Coupon Not Updated" Response"

#### 6] Delete coupon By ID
```http
  DELETE /coupons/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of coupon to Delete |

- **On Success** : will give "Coupon Deleted Successfully" reponse 
- **Limitation** : dont try to delete Coupon which dont exist ,doing so will give you "Coupon Not Deleted Successfully" Response

#### 7] Apply coupon By ID
```http
  DELETE /coupons/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of coupon to Delete |

- Apply a specific coupon to the cart and return the updated cart with discounted prices for each item.

- RequestBody For the Cart Should Look like:
  ```json
      {
          "cart": {
                "items": [
                    {"product_id": 1, "quantity": 6, "price": 50}, // Product X
                    {"product_id": 2, "quantity": 3, "price": 30}, // Product Y
                    {"product_id": 3, "quantity": 2, "price": 25} // Product Z
                ]
           }
      }
  ```

- expected output:
  ```json
    {
        "updated_cart": {
            "items": [
                {
                    "quantity": 6,
                    "total_discount": 24.0,
                    "price": 50,
                    "product_id": 1
                },
                {
                    "quantity": 3,
                    "total_discount": 12.0,
                    "price": 30,
                    "product_id": 2
                },
                {
                    "quantity": 2,
                    "total_discount": 8.0,
                    "price": 25,
                    "product_id": 3
                }
            ],
            "total_price": 440,
            "total_discount": 44,
            "final_price": 396
        }
    }
  ```
- **Limitation** : for bxgy offer will be applied to only one get_products suppose you have added 2 free product.

- **Note** : Im Using Double(float) for total_discount because had faced scenario where the sum didn't match the total Discounted Price
---

###### NOTE: Have Imlemented 2 more API's to GET and DELETE Product , this was done so becouse once a coupon is deleted product become disassociated from Coupon,and suppose if future you are not going to supply that product ,you can delete the product from DB entirely.
---

#### 8] Delete product By ID
```http
  DELETE /product/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of product to Delete |

- **On Succcess you will Get** : "Product Deleted Successfully"
- **Limitations**:
  - cannot delete product which dont exist, doing so will give you "Product Not Deleted"    Response.
  - cannot delete product which is associated to Coupon, doing so will give you :
    "This Product is Associated to a Coupon and can't be deleted".

#### 8] Get All product
```http
  GET /products
```
- To get all the Product

---

##### Editor Tool Used to create this **README**

https://dillinger.io/

---
##### This Application is the Solution provided for the problem : https://drive.google.com/file/d/125I_3GmENCluttfHgynrHCvV7pe1JRan/view
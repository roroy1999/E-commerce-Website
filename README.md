# Coupons Management API for E-commerce-Webs

Introduction : This is simple Coupons Management application were you can add ,apply or delete coupon ,there are mainly 3 typs of Coupons supported as of now, which are
               1. Cart-wise Coupons:
                  ● 10% off on carts over Rs. 100
                    ○ Condition: Cart total > 100
                    ○ Discount: 10% off the entire cart
```
                          {
                              "type": "cart-wise",
                              "details": {
                                  "threshold": 100,
                                  "discount": 10
                              }
                          }
```
                    

              2. Product-wise Coupons:
                 ● 20% off on Product A
                    ○ Condition: Product A is in the cart
                    ○ Discount: 20% off Product A
```  
                          {
                              "type": "product-wise",
                                "details": {            
                                    "product_id": 1,
                                    "discount": 20
                                }
                          }
```
              3. BxGy Coupons:
                ● Buy a specified number of products from one array and get a specified number of
                products from another array for free.
                    ○ Example: b2g1 - Buy 2 products from the “buy” array (e.g., [X, Y, Z]) and
                    get 1 product from the “get” array (e.g., [A, B, C]) for free.
                
                ● Scenarios:
                    ○ If the cart has products X, Y, and A, then ‘A’ would be free.
                    ○ If the cart has products X, Z, and C, then ‘C’ would be free.
                    ○ If the cart has products X, A, and B, the b2g1 coupon is not applicable as
                    there are not 2 products from the “buy” array.
                
                ● Repetition Limit: If the repetition limit is 3, the coupon can be applied 3 times.
                    ○ If the cart has 6 products from the “buy” array and 3 products from the
                    “get” array, the coupon can be applied 3 times. I.e. for b2g1, buying 6
                    products from [X, Y, Z] would result in getting 3 products from [A, B, C] for
                    free.
                    ○ If the cart has products [X, X, X, Y, Y, Y] (6 items from the “buy” array)
                    and products [A, B, C] or [A,B], then [A, B, and C] or [A,B] would be free.
``` 
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
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Prerequisites:
1] Java JDK installed
2] Maven
3] Docker(if you dont have Postgress Local Setup)
4] PostMan(if not installed ,Online one can be used through Browser)

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                    

Before we go to the API Part if you want to test out this Application ,there are 2 ways to do so:
1] Run the Application as Spring Boot App and connect it to DB which you have setup Locally.
                                         or
2] Have given Docker Compose Support, so if you don't have DB setup in your local machine and Docker is installed ,you can run this application by running the below CMD:
   ```
   docker compose up -d
   ```
   and to stop:
   ```
   docker compose down
   ```

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

API Implementaion :
● POST /coupons: Create a new coupon.

  Using This API you can save cart-wise or product-wise or bxgy coupon

  Request Body to add the Coupon is as Follows :

  To Save cart-wise
```
      {
          "type": "cart-wise",
          "details": {
              "threshold": 100,
              "discount": 10
          }
      }
```
  To Save product-wise
```  
      {
          "type": "product-wise",
            "details": {            
                "product_id": 1,
                "discount": 20
            }
      }
```
  To Save bxgy
``` 
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
   On Successful Addition you will be Displayed :
   "Coupon is Added" or "Product Discount was Added"

● GET /coupons: Retrieve all coupons.
  using this you can fetch all the Coupon's which was added until now
  
● GET /coupons/{id}: By Passing the ID you can fetch any specific coupon
  Limitation : CouponId you are quering should exist else you will get response as "Coupon Not Found"

● POST /applicable-coupons: Fetch all applicable coupons for a given cart and
  calculate the total discount that will be applied by each coupon.

  RequestBody For the Cart Should Look like:
  ```
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

  expected Response :
  ```
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
● PUT /coupons/{id}: Update a specific coupon by its ID.
    Request Body to add the Coupon is as Follows :

    To Update cart-wise
```
      {
          "type": "cart-wise",
          "details": {
              "threshold": 100,
              "discount": 10
          }
      }
```
    To Update product-wise
```  
      {
          "type": "product-wise",
            "details": {            
                "product_id": 1,
                "discount": 20
            }
      }
```
    To Update bxgy
``` 
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
   On Success : will give "Coupon Updated Successfully" reponse 
   Limitation : dont give Coupon which dont exist ,doing so will give you "Coupon Not Updated" Response


● DELETE /coupons/{id}: Delete a specific coupon by its ID.
  On Success : will give "Coupon Deleted Successfully" reponse 
  Limitation : dont try to delete Coupon which dont exist ,doing so will give you "Coupon Not Deleted Successfully" Response

● POST /apply-coupon/{id}: Apply a specific coupon to the cart and return the
  updated cart with discounted prices for each item.

  RequestBody For the Cart Should Look like:
  ```
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

expected output:
```
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

  Note : Im Using Double(float) for total_discount because had faced scenario where the sum didn't match the total Discounted Price
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  
  Have Imlemented 2 more API's to GET and DELETE Product , this was done so becouse once a coupon is deleted product become disassociated from Coupon,
  and suppose if future you are not going to supply that product ,you can delete the product from DB entirely.
● DELETE /product/{id}: Delete a specific product by its ID.

  On Succcess you will Get : "Product Deleted Successfully"

  Limitations:
  1] cannot delete product which dont exist, doing so will give you "Product Not Deleted" Response.
  2] cannot delete product which is associated to Coupon, doing so will give you "This Product is Associated to a Coupon and can't be deleted".

● GET /products: To get all the Product
  

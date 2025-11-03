# Hotel Room Allocation System

## Problem Statement

Build a room allocation optimization tool for a hotel! The hotel has limited rooms available each night and multiple guests who want to book rooms at different price points.

## Business Rules

The hotel has two types of rooms:
1. **Premium Rooms** - For high-paying guests (EUR 100+)
2. **Economy Rooms** - For budget guests (below EUR 100)

### Room Allocation Logic:

1. **Premium guests (≥ EUR 100)** will ONLY be allocated Premium rooms
2. **Economy guests (< EUR 100)** will be allocated Economy rooms by default
3. **Smart Upgrade**: If Premium rooms are empty AND Economy rooms are full, lower-paying guests can be upgraded to Premium rooms
   - The highest-paying Economy guests get upgraded first
4. **Overbooking scenario**: When there are more guests than rooms, only the highest-paying guests get rooms

Please build a small API that provides an interface for hotels to enter the numbers of Premium and Economy rooms that are available for the night. 
API should tell them immediately how many rooms of each category will be occupied and how much money they will make in total. 
Potential guests are represented by an array of numbers that is their willingness to pay for the night.

Use the following raw JSON file/structure as mock data for potential guests in your tests:

```json
[23, 45, 155, 374, 22, 99.99, 100, 101, 115, 209]
```

## Requirements for a valid solution

* Prioritize Code Quality, Readability and Clarity
    * Maintain a clean code structure and formatting
    * Use thoughtful naming for variables and functions
    * Focus on good engineering practices and good application architecture
* Java Version: Use the most modern version of Java you are comfortable with.
* Version Control: Track your progress through Git commits.
* Testing: At minimum implement tests as specified in the provided test cases.
* Web Framework: Use Spring Boot to expose the API.
    * Make sure the `run.sh` script in yours repository root directory __builds__ and __starts__ the application. 
      * Your script will be executed inside the Docker container (`eclipse-temurin:21-jdk-jammy` - Ubuntu v22.04). 
      * We will run automated tests against the running service. 
      * Do *not* use `docker` or `docker-compose` inside the `run.sh` script. 
    * Make sure the application starts on port `8080`.
    * Make sure to implement the REST API as a POST request to `/occupancy` with:
        * Input:

        ```json
        {
            "premiumRooms": 7,
            "economyRooms": 5,
            "potentialGuests": [23, 45, 155, 374, 22, 99.99, 100, 101, 115, 209]
        }
        ```

        * Output:

        ```json
        {
            "usagePremium": 6,
            "revenuePremium": 1054,
            "usageEconomy": 4,
            "revenueEconomy": 189.99
        }
        ```

* Provide the URL of your __private__ GitHub repository. __Do not publish__ our Github fine-grained token in the repository. Consider using:
    * [Introducing fine-grained personal access tokens for GitHub](https://github.blog/2022-10-18-introducing-fine-grained-personal-access-tokens-for-github/)
    * [GitLab - Personal access tokens](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)

## Tests

1. ```text
   (input) Free Premium rooms: 3
   (input) Free Economy rooms: 3
   (output) Usage Premium: 3 (EUR 738)
   (output) Usage Economy: 3 (EUR 167.99)
   ```

2. ```text
   (input) Free Premium rooms: 7
   (input) Free Economy rooms: 5
   (output) Usage Premium: 6 (EUR 1054)
   (output) Usage Economy: 4 (EUR 189.99)
   ```

3. ```text
   (input) Free Premium rooms: 2
   (input) Free Economy rooms: 7
   (output) Usage Premium: 2 (EUR 583)
   (output) Usage Economy: 4 (EUR 189.99)
   ```

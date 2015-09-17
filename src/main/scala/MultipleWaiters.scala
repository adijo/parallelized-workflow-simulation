import akka.actor._

case class SpecialOrder(number : Integer, customer : ActorRef)

class SpecialCustomer(loadBalancer : ActorRef) extends Actor {
    
      var currentOrder = 1
      def receive = {
        case Food =>
          println(self.path.name + ": Thanks for that.")
          currentOrder += 1
          loadBalancer ! SpecialOrder(currentOrder, self)
        case Initiate =>
          loadBalancer ! SpecialOrder(currentOrder, self)
      }
  }

class SpecialWaiter extends Actor {
     
     // 10 orders per person.
      val limit = 5
      def receive = {
        case SpecialOrder(number : Integer, customer : ActorRef) =>
            if(number > limit) customer ! PoisonPill
            else {
              println("Waiter: #" + self.path.name + " Here's order # " +
                  number + " " + customer.path.name)
              customer ! Food
            }
      }
  }

class LoadBalancer(numWaiters : Integer) extends Actor {
  
   /*
    * Load balancer for restaurant waiters.
    * Will distribute load with equal probability 
    * to all the waiters. Good performance in expectation.
    */
  
    val waitersList = (for(waiter <- 1 to numWaiters) 
      yield(context.actorOf(Props[SpecialWaiter], name = "waiter" + waiter)))

    val waiters = waitersList.toArray
    val r = scala.util.Random
    
    def receive = {
      case SpecialOrder(number : Integer, customer : ActorRef) =>
        getRandomWaiter ! SpecialOrder(number, customer)
    }
    
    def getRandomWaiter =  waiters(r.nextInt(waiters.size)) 
}

  object ComplexWorkflow extends App {
      val customerLimit = 2
      val system = ActorSystem("RestaurantSystemLoadBalancer")
      val loadBalancer = system.actorOf(Props(new LoadBalancer(5)), name = "loadbalancer")
      val customers = for(i <- 1 to customerLimit) 
        yield system.actorOf(Props(new SpecialCustomer(loadBalancer)), name = "customer" + i)
      customers.foreach { x => x ! Initiate }
  }
  



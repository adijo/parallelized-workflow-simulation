import akka.actor._

case class Order(number : Integer)
case object Food
case object Initiate


class Waiter extends Actor {
     
     // 10 orders per person.
      val limit = 2
      def receive = {
        case Order(number : Integer) =>
            if(number > limit) sender ! PoisonPill
            else {
              println("Waiter: #" + self.path.name + " Here's order # " +
                  number + " " + sender.path.name)
              sender ! Food
            }
      }
  }

class NormalCustomer(waiter : ActorRef) extends Actor {
    
      var currentOrder = 1
      def receive = {
        case Food =>
          println("Thanks for that.")
          currentOrder += 1
          sender ! Order(currentOrder)
        case Initiate =>
          waiter ! Order(currentOrder)
      }
  }

  object Workflow extends App {
      val customerLimit = 2
      val system = ActorSystem("RestaurantSystem")
      val waiter = system.actorOf(Props[Waiter], name = "waiter")
      val customers = for(i <- 1 to customerLimit) 
        yield system.actorOf(Props(new NormalCustomer(waiter)), name = "customer" + i)
      customers.foreach { x => x ! Initiate }
  }
  

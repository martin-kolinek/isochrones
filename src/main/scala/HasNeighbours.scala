trait HasNeighbours[T] {
	def neighbours(node:T) : Traversable[(T, Double)]
}